package com.aichat.provider.anthropic

import com.aichat.data.model.Message
import com.aichat.data.model.ProviderConfig
import com.aichat.provider.MessageDto
import com.aichat.provider.ProviderAdapter
import com.aichat.provider.StreamEvent
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class AnthropicAdapter : ProviderAdapter {
    private val gson = Gson()
    private var currentEventSource: EventSource? = null

    override suspend fun sendMessage(
        config: ProviderConfig,
        messages: List<Message>,
        onStreamEvent: (StreamEvent) -> Unit
    ) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(config.timeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(config.timeout.toLong(), TimeUnit.SECONDS)
            .build()

        val messageDtos = messages.filter { it.role.value != "system" }.map { 
            MessageDto(role = it.role.value, content = it.content) 
        }

        val requestBody = JsonObject().apply {
            addProperty("model", config.model)
            add("messages", gson.toJsonTree(messageDtos))
            addProperty("stream", true)
            addProperty("max_tokens", config.maxTokens ?: 4096)
            config.temperature?.let { addProperty("temperature", it) }
            config.topP?.let { addProperty("top_p", it) }
        }

        val url = "${config.baseUrl.trimEnd('/')}/v1/messages"
        val request = Request.Builder()
            .url(url)
            .header("x-api-key", config.apiKeyEnc)
            .header("anthropic-version", config.anthropicVersion)
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val fullTextBuilder = StringBuilder()

        currentEventSource = EventSources.createFactory(client).newEventSource(
            request,
            object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                    if (!response.isSuccessful) {
                        val errorMsg = response.body?.string() ?: "Unknown error"
                        val errorCode = when (response.code) {
                            401, 403 -> "AUTH_ERROR"
                            404 -> "NOT_FOUND"
                            429 -> "RATE_LIMIT"
                            in 500..599 -> "SERVER_ERROR"
                            else -> "HTTP_ERROR"
                        }
                        onStreamEvent(StreamEvent.Error(errorCode, errorMsg))
                        return
                    }
                    onStreamEvent(StreamEvent.Start())
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    try {
                        val json = gson.fromJson(data, JsonObject::class.java)
                        val eventType = json.get("type")?.asString

                        when (eventType) {
                            "content_block_delta" -> {
                                val delta = json.getAsJsonObject("delta")
                                val text = delta?.get("text")?.asString
                                text?.let {
                                    fullTextBuilder.append(it)
                                    onStreamEvent(StreamEvent.Delta(it))
                                }
                            }
                            "message_stop" -> {
                                onStreamEvent(StreamEvent.Complete(fullTextBuilder.toString()))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: okhttp3.Response?
                ) {
                    val errorMsg = t?.message ?: response?.message ?: "Unknown error"
                    onStreamEvent(StreamEvent.Error("NETWORK_ERROR", errorMsg))
                }

                override fun onClosed(eventSource: EventSource) {
                    currentEventSource = null
                }
            }
        )
    }

    override suspend fun testConnection(config: ProviderConfig): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

                val testBody = JsonObject().apply {
                    addProperty("model", config.model)
                    add("messages", gson.toJsonTree(listOf(
                        mapOf("role" to "user", "content" to "Hi")
                    )))
                    addProperty("max_tokens", 10)
                }

                val url = "${config.baseUrl.trimEnd('/')}/v1/messages"
                val request = Request.Builder()
                    .url(url)
                    .header("x-api-key", config.apiKeyEnc)
                    .header("anthropic-version", config.anthropicVersion)
                    .header("Content-Type", "application/json")
                    .post(testBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success("连接成功")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun cancel() {
        currentEventSource?.cancel()
        currentEventSource = null
    }
}
