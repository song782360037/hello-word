package com.aichat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val isStreaming: Boolean = false,
    val isComplete: Boolean = true,
    val errorCode: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MessageRole(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    companion object {
        fun fromValue(value: String): MessageRole = 
            values().find { it.value == value } ?: USER
    }
}
