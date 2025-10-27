package com.aichat.data.repository

import com.aichat.data.local.ConversationDao
import com.aichat.data.local.MessageDao
import com.aichat.data.local.ProviderConfigDao
import com.aichat.data.local.WebDAVSettingsDao
import com.aichat.data.model.Conversation
import com.aichat.data.model.Message
import com.aichat.data.model.ProviderConfig
import com.aichat.data.model.WebDAVSettings
import com.aichat.data.remote.WebDAVClient
import com.aichat.util.CryptoUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WebDAVRepository(
    private val webDAVSettingsDao: WebDAVSettingsDao,
    private val providerConfigDao: ProviderConfigDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val cryptoUtil: CryptoUtil
) {
    private val webDAVClient = WebDAVClient()
    private val gson = Gson()

    fun getSettings(): Flow<WebDAVSettings?> = webDAVSettingsDao.getSettings()

    suspend fun saveSettings(settings: WebDAVSettings) {
        cryptoUtil.encrypt(CryptoUtil.webdavPasswordKey(), settings.passwordEnc)
        webDAVSettingsDao.insert(settings.copy(passwordEnc = "encrypted"))
    }

    suspend fun getDecryptedSettings(): WebDAVSettings? {
        val settings = webDAVSettingsDao.getSettingsOnce() ?: return null
        val decryptedPassword = cryptoUtil.decrypt(CryptoUtil.webdavPasswordKey()) ?: ""
        return settings.copy(passwordEnc = decryptedPassword)
    }

    suspend fun testConnection(): Result<Unit> {
        val settings = getDecryptedSettings() ?: return Result.failure(Exception("WebDAV not configured"))
        return webDAVClient.testConnection(settings)
    }

    suspend fun uploadData(): Result<Unit> {
        val settings = getDecryptedSettings() ?: return Result.failure(Exception("WebDAV not configured"))

        val configs = providerConfigDao.getAllConfigs().first().map { config ->
            if (settings.includeApiKeys) {
                val apiKey = cryptoUtil.decrypt(CryptoUtil.encryptionKey(config.id)) ?: ""
                config.copy(apiKeyEnc = apiKey)
            } else {
                config.copy(apiKeyEnc = "")
            }
        }

        val conversations = conversationDao.getAllConversations().first()
        val allMessages = mutableListOf<Message>()
        conversations.forEach { conv ->
            val messages = messageDao.getMessages(conv.id).first()
            allMessages.addAll(messages)
        }

        val configJson = gson.toJson(configs)
        val configResult = webDAVClient.uploadFile(settings, "config.json", configJson)
        if (configResult.isFailure) return configResult

        val dataMap = mapOf(
            "conversations" to conversations,
            "messages" to allMessages
        )
        val dataJson = gson.toJson(dataMap)
        val dataResult = webDAVClient.uploadFile(settings, "conversations.json", dataJson)

        return dataResult
    }

    suspend fun downloadData(): Result<Unit> {
        val settings = getDecryptedSettings() ?: return Result.failure(Exception("WebDAV not configured"))

        val configResult = webDAVClient.downloadFile(settings, "config.json")
        if (configResult.isSuccess) {
            val configJson = configResult.getOrNull() ?: ""
            val type = object : TypeToken<List<ProviderConfig>>() {}.type
            val configs: List<ProviderConfig> = gson.fromJson(configJson, type)
            
            configs.forEach { config ->
                if (config.apiKeyEnc.isNotEmpty()) {
                    cryptoUtil.encrypt(CryptoUtil.encryptionKey(config.id), config.apiKeyEnc)
                }
                providerConfigDao.insert(config.copy(apiKeyEnc = "encrypted"))
            }
        }

        val dataResult = webDAVClient.downloadFile(settings, "conversations.json")
        if (dataResult.isSuccess) {
            val dataJson = dataResult.getOrNull() ?: ""
            val type = object : TypeToken<Map<String, List<Any>>>() {}.type
            val dataMap: Map<String, List<*>> = gson.fromJson(dataJson, type)

            val conversationsJson = gson.toJson(dataMap["conversations"])
            val conversationType = object : TypeToken<List<Conversation>>() {}.type
            val conversations: List<Conversation> = gson.fromJson(conversationsJson, conversationType)

            val messagesJson = gson.toJson(dataMap["messages"])
            val messageType = object : TypeToken<List<Message>>() {}.type
            val messages: List<Message> = gson.fromJson(messagesJson, messageType)

            conversations.forEach { conversationDao.insert(it) }
            messages.forEach { messageDao.insert(it) }
        }

        return Result.success(Unit)
    }
}
