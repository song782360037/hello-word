package com.aichat.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.data.model.ProviderConfig
import com.aichat.data.model.ProviderId
import com.aichat.data.model.WebDAVSettings
import com.aichat.data.repository.ChatRepository
import com.aichat.data.repository.WebDAVRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as com.aichat.AIChatApplication
    private val chatRepository = app.chatRepository
    private val webDAVRepository = app.webdavRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _providerConfigs = MutableStateFlow<Map<String, ProviderConfig>>(emptyMap())
    val providerConfigs: StateFlow<Map<String, ProviderConfig>> = _providerConfigs.asStateFlow()

    private val _webdavSettings = MutableStateFlow<WebDAVSettings?>(null)
    val webdavSettings: StateFlow<WebDAVSettings?> = _webdavSettings.asStateFlow()

    init {
        loadConfigs()
        loadWebDAVSettings()
    }

    private fun loadConfigs() {
        viewModelScope.launch {
            ProviderId.values().forEach { provider ->
                val config = chatRepository.getProviderConfig(provider.id)
                if (config != null) {
                    _providerConfigs.value = _providerConfigs.value + (provider.id to config)
                }
            }
        }
    }

    private fun loadWebDAVSettings() {
        viewModelScope.launch {
            webDAVRepository.getSettings().collect { settings ->
                _webdavSettings.value = settings
            }
        }
    }

    fun saveProviderConfig(config: ProviderConfig) {
        viewModelScope.launch {
            chatRepository.saveProviderConfig(config)
            _providerConfigs.value = _providerConfigs.value + (config.id to config)
            _uiState.value = _uiState.value.copy(message = "配置已保存")
        }
    }

    fun testConnection(providerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.testConnection(providerId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = if (result.isSuccess) {
                    result.getOrNull() ?: "连接成功"
                } else {
                    "连接失败: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun saveWebDAVSettings(settings: WebDAVSettings) {
        viewModelScope.launch {
            webDAVRepository.saveSettings(settings)
            _uiState.value = _uiState.value.copy(message = "WebDAV 设置已保存")
        }
    }

    fun testWebDAVConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = webDAVRepository.testConnection()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = if (result.isSuccess) {
                    "WebDAV 连接成功"
                } else {
                    "WebDAV 连接失败: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun uploadToWebDAV() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = webDAVRepository.uploadData()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = if (result.isSuccess) {
                    "上传成功"
                } else {
                    "上传失败: ${result.exceptionOrNull()?.message}"
                }
            )
        }
    }

    fun downloadFromWebDAV() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = webDAVRepository.downloadData()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = if (result.isSuccess) {
                    "下载成功"
                } else {
                    "下载失败: ${result.exceptionOrNull()?.message}"
                }
            )
            loadConfigs()
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null
)
