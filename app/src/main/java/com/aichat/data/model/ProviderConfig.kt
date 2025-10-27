package com.aichat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configs")
data class ProviderConfig(
    @PrimaryKey
    val id: String,
    val baseUrl: String,
    val apiKeyEnc: String,
    val model: String,
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxTokens: Int? = null,
    val timeout: Int = 60,
    val anthropicVersion: String = "2023-06-01",
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ProviderId(val id: String, val displayName: String) {
    OPENAI("openai", "OpenAI"),
    GEMINI("gemini", "Gemini"),
    ANTHROPIC("anthropic", "Anthropic");

    companion object {
        fun fromId(id: String): ProviderId? = values().find { it.id == id }
    }
}
