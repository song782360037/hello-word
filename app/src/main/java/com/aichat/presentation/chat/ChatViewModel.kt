package com.aichat.presentation.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.data.model.Conversation
import com.aichat.data.model.Message
import com.aichat.data.model.MessageRole
import com.aichat.data.model.ProviderId
import com.aichat.data.repository.ChatRepository
import com.aichat.provider.StreamEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as com.aichat.AIChatApplication).chatRepository

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentConversationId: String? = null

    init {
        createNewConversation()
    }

    fun createNewConversation() {
        val providerId = _uiState.value.selectedProvider
        viewModelScope.launch {
            val config = repository.getProviderConfig(providerId)
            if (config == null) {
                _uiState.value = _uiState.value.copy(
                    error = "请先配置 $providerId"
                )
                return@launch
            }

            val conversation = Conversation(
                title = "新对话",
                providerId = providerId,
                model = config.model
            )
            repository.createConversation(conversation)
            currentConversationId = conversation.id
            
            viewModelScope.launch {
                repository.getMessages(conversation.id).collect { messageList ->
                    _messages.value = messageList
                }
            }
        }
    }

    fun loadConversation(conversationId: String) {
        currentConversationId = conversationId
        viewModelScope.launch {
            val conversation = repository.getConversation(conversationId)
            conversation.collect { conv ->
                conv?.let {
                    _uiState.value = _uiState.value.copy(
                        selectedProvider = it.providerId
                    )
                }
            }
            
            repository.getMessages(conversationId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }

    fun selectProvider(providerId: String) {
        _uiState.value = _uiState.value.copy(selectedProvider = providerId)
        createNewConversation()
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        val conversationId = currentConversationId ?: run {
            createNewConversation()
            currentConversationId
        } ?: return

        viewModelScope.launch {
            val userMessage = Message(
                conversationId = conversationId,
                role = MessageRole.USER,
                content = content
            )
            repository.insertMessage(userMessage)

            val assistantMessage = Message(
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                content = "",
                isStreaming = true,
                isComplete = false
            )
            repository.insertMessage(assistantMessage)

            _uiState.value = _uiState.value.copy(
                isStreaming = true,
                error = null
            )

            val allMessages = _messages.value
            val contentBuilder = StringBuilder()

            try {
                repository.sendMessage(
                    providerId = _uiState.value.selectedProvider,
                    messages = allMessages.filter { it.id != assistantMessage.id },
                    onStreamEvent = { event ->
                        viewModelScope.launch {
                            when (event) {
                                is StreamEvent.Start -> {
                                    contentBuilder.clear()
                                }
                                is StreamEvent.Delta -> {
                                    contentBuilder.append(event.text)
                                    repository.updateMessage(
                                        assistantMessage.copy(
                                            content = contentBuilder.toString()
                                        )
                                    )
                                }
                                is StreamEvent.Complete -> {
                                    repository.updateMessage(
                                        assistantMessage.copy(
                                            content = event.fullText,
                                            isStreaming = false,
                                            isComplete = true
                                        )
                                    )
                                    _uiState.value = _uiState.value.copy(isStreaming = false)
                                    
                                    if (_messages.value.size <= 2) {
                                        val title = content.take(20) + if (content.length > 20) "..." else ""
                                        val conv = repository.getConversation(conversationId)
                                        conv.collect { conversation ->
                                            conversation?.let {
                                                repository.updateConversation(it.copy(title = title))
                                            }
                                        }
                                    }
                                }
                                is StreamEvent.Error -> {
                                    repository.updateMessage(
                                        assistantMessage.copy(
                                            content = "错误: ${event.message}",
                                            isStreaming = false,
                                            isComplete = false,
                                            errorCode = event.code
                                        )
                                    )
                                    _uiState.value = _uiState.value.copy(
                                        isStreaming = false,
                                        error = event.message
                                    )
                                }
                                is StreamEvent.Cancel -> {
                                    repository.updateMessage(
                                        assistantMessage.copy(
                                            content = contentBuilder.toString(),
                                            isStreaming = false,
                                            isComplete = false
                                        )
                                    )
                                    _uiState.value = _uiState.value.copy(isStreaming = false)
                                }
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                repository.updateMessage(
                    assistantMessage.copy(
                        content = "错误: ${e.message}",
                        isStreaming = false,
                        isComplete = false,
                        errorCode = "EXCEPTION"
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = e.message
                )
            }
        }
    }

    fun stopStreaming() {
        repository.cancelStream(_uiState.value.selectedProvider)
        _uiState.value = _uiState.value.copy(isStreaming = false)
    }

    fun retry() {
        val lastUserMessage = _messages.value.lastOrNull { it.role == MessageRole.USER }
        lastUserMessage?.let {
            sendMessage(it.content)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ChatUiState(
    val selectedProvider: String = ProviderId.OPENAI.id,
    val isStreaming: Boolean = false,
    val error: String? = null
)
