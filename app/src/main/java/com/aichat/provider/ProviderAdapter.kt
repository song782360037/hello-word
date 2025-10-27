package com.aichat.provider

import com.aichat.data.model.Message
import com.aichat.data.model.ProviderConfig
import kotlinx.coroutines.flow.Flow

interface ProviderAdapter {
    suspend fun sendMessage(
        config: ProviderConfig,
        messages: List<Message>,
        onStreamEvent: (StreamEvent) -> Unit
    )

    suspend fun testConnection(config: ProviderConfig): Result<String>
}

sealed class StreamEvent {
    data class Start(val requestId: String = "") : StreamEvent()
    data class Delta(val text: String) : StreamEvent()
    data class Complete(val fullText: String) : StreamEvent()
    data class Error(val code: String, val message: String) : StreamEvent()
    object Cancel : StreamEvent()
}

data class SendMessageRequest(
    val messages: List<MessageDto>,
    val stream: Boolean = true,
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxTokens: Int? = null
)

data class MessageDto(
    val role: String,
    val content: String
)
