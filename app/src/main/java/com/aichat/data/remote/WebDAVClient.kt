package com.aichat.data.remote

import com.aichat.data.model.WebDAVSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class WebDAVClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun uploadFile(
        settings: WebDAVSettings,
        fileName: String,
        content: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "${settings.url.trimEnd('/')}/${settings.remotePath.trim('/')}/$fileName"
            val credentials = Credentials.basic(settings.username, settings.passwordEnc)

            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .put(content.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 201 || response.code == 204) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Upload failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(
        settings: WebDAVSettings,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${settings.url.trimEnd('/')}/${settings.remotePath.trim('/')}/$fileName"
            val credentials = Credentials.basic(settings.username, settings.passwordEnc)

            val request = Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val content = response.body?.string() ?: ""
                Result.success(content)
            } else {
                Result.failure(Exception("Download failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(settings: WebDAVSettings): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                val credentials = Credentials.basic(settings.username, settings.passwordEnc)
                val request = Request.Builder()
                    .url(settings.url)
                    .header("Authorization", credentials)
                    .method("PROPFIND", "".toRequestBody())
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful || response.code == 207) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Connection failed: ${response.code} ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
