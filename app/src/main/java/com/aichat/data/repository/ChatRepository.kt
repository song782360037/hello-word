package com.aichat.data.repository

import com.aichat.data.local.ConversationDao
import com.aichat.data.local.MessageDao
import com.aichat.data.local.ProviderConfigDao
import com.aichat.data.model.Conversation
import com.aichat.data.model.Message
import com.aichat.data.model.ProviderConfig
import com.aichat.provider.ProviderAdapter
import com.aichat.provider.StreamEvent
import com.aichat.provider.anthropic.AnthropicAdapter
import com.aichat.provider.gemini.GeminiAdapter
import com.aichat.provider.openai.OpenAIAdapter
import com.aichat.util.CryptoUtil
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val providerConfigDao: ProviderConfigDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val cryptoUtil: CryptoUtil
) {
    private val openAIAdapter = OpenAIAdapter()
    private val geminiAdapter = GeminiAdapter()
    private val anthropicAdapter = AnthropicAdapter()

    fun getAllConversations(): Flow<List<Conversation>> = 
        conversationDao.getAllConversations()

    fun getConversation(conversationId: String): Flow<Conversation?> =
        conversationDao.getConversationFlow(conversationId)

    suspend fun createConversation(conversation: Conversation) {
        conversationDao.insert(conversation)
    }

    suspend fun updateConversation(conversation: Conversation) {
        conversationDao.update(conversation)
    }

    suspend fun deleteConversation(conversationId: String) {
        messageDao.deleteByConversation(conversationId)
        conversationDao.delete(conversationId)
    }

    suspend fun deleteAllConversations() {
        messageDao.deleteAll()
        conversationDao.deleteAll()
    }

    fun getMessages(conversationId: String): Flow<List<Message>> =
        messageDao.getMessages(conversationId)

    suspend fun insertMessage(message: Message) {
        messageDao.insert(message)
    }

    suspend fun updateMessage(message: Message) {
        messageDao.update(message)
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.delete(messageId)
    }

    suspend fun getProviderConfig(providerId: String): ProviderConfig? {
        val config = providerConfigDao.getConfig(providerId) ?: return null
        val decryptedApiKey = cryptoUtil.decrypt(CryptoUtil.encryptionKey(providerId)) ?: ""
        return config.copy(apiKeyEnc = decryptedApiKey)
    }

    fun getAllProviderConfigs(): Flow<List<ProviderConfig>> =
        providerConfigDao.getAllConfigs()

    suspend fun saveProviderConfig(config: ProviderConfig) {
        cryptoUtil.encrypt(CryptoUtil.encryptionKey(config.id), config.apiKeyEnc)
        providerConfigDao.insert(config.copy(apiKeyEnc = "encrypted"))
    }

    suspend fun sendMessage(
        providerId: String,
        messages: List<Message>,
        onStreamEvent: (StreamEvent) -> Unit
    ) {
        val config = getProviderConfig(providerId) ?: throw IllegalStateException("Provider not configured")
        val adapter = getAdapter(providerId)
        adapter.sendMessage(config, messages, onStreamEvent)
    }

    suspend fun testConnection(providerId: String): Result<String> {
        val config = getProviderConfig(providerId) ?: return Result.failure(Exception("Provider not configured"))
        val adapter = getAdapter(providerId)
        return adapter.testConnection(config)
    }

    fun cancelStream(providerId: String) {
        when (providerId) {
            "openai" -> openAIAdapter.cancel()
            "gemini" -> geminiAdapter.cancel()
            "anthropic" -> anthropicAdapter.cancel()
        }
    }

    private fun getAdapter(providerId: String): ProviderAdapter {
        return when (providerId) {
            "openai" -> openAIAdapter
            "gemini" -> geminiAdapter
            "anthropic" -> anthropicAdapter
            else -> throw IllegalArgumentException("Unknown provider: $providerId")
        }
    }
}
