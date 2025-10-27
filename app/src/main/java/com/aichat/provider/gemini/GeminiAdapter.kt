package com.aichat.provider.gemini

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

class GeminiAdapter : ProviderAdapter {
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

        val contents = messages.filter { it.role.value != "system" }.map { message ->
            JsonObject().apply {
                addProperty("role", if (message.role.value == "assistant") "model" else message.role.value)
                add("parts", gson.toJsonTree(listOf(mapOf("text" to message.content))))
            }
        }

        val requestBody = JsonObject().apply {
            add("contents", gson.toJsonTree(contents))
            val generationConfig = JsonObject().apply {
                config.temperature?.let { addProperty("temperature", it) }
                config.topP?.let { addProperty("topP", it) }
                config.maxTokens?.let { addProperty("maxOutputTokens", it) }
            }
            add("generationConfig", generationConfig)
        }

        val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.model}:streamGenerateContent?key=${config.apiKeyEnc}&alt=sse"
        val request = Request.Builder()
            .url(url)
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
                            400, 401, 403 -> "AUTH_ERROR"
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
                        val candidates = json.getAsJsonArray("candidates")
                        
                        candidates?.forEach { candidate ->
                            val content = candidate.asJsonObject
                                .getAsJsonObject("content")
                                ?.getAsJsonArray("parts")

                            content?.forEach { part ->
                                val text = part.asJsonObject.get("text")?.asString
                                text?.let {
                                    fullTextBuilder.append(it)
                                    onStreamEvent(StreamEvent.Delta(it))
                                }
                            }
                        }

                        val finishReason = candidates?.get(0)?.asJsonObject?.get("finishReason")?.asString
                        if (finishReason != null) {
                            onStreamEvent(StreamEvent.Complete(fullTextBuilder.toString()))
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

                val url = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.model}?key=${config.apiKeyEnc}"
                val request = Request.Builder()
                    .url(url)
                    .get()
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
