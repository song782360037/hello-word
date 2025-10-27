package com.aichat.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aichat.R
import com.aichat.data.model.ProviderConfig
import com.aichat.data.model.ProviderId
import com.aichat.data.model.WebDAVSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val providerConfigs by viewModel.providerConfigs.collectAsState()
    val webdavSettings by viewModel.webdavSettings.collectAsState()
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_tab)) }
        )

        if (uiState.message != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearMessage() }) {
                        Text("关闭")
                    }
                }
            ) {
                Text(uiState.message ?: "")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProviderId.values().forEach { provider ->
                ProviderConfigSection(
                    provider = provider,
                    config = providerConfigs[provider.id],
                    onSave = { viewModel.saveProviderConfig(it) },
                    onTest = { viewModel.testConnection(provider.id) },
                    isLoading = uiState.isLoading
                )
            }

            HorizontalDivider()

            WebDAVSettingsSection(
                settings = webdavSettings,
                onSave = { viewModel.saveWebDAVSettings(it) },
                onTest = { viewModel.testWebDAVConnection() },
                onUpload = { viewModel.uploadToWebDAV() },
                onDownload = { viewModel.downloadFromWebDAV() },
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
fun ProviderConfigSection(
    provider: ProviderId,
    config: ProviderConfig?,
    onSave: (ProviderConfig) -> Unit,
    onTest: () -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var apiKey by remember(config) { mutableStateOf(config?.apiKeyEnc ?: "") }
    var baseUrl by remember(config) { mutableStateOf(config?.baseUrl ?: "") }
    var model by remember(config) { mutableStateOf(config?.model ?: "") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = provider.displayName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text(stringResource(R.string.api_key)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text(stringResource(R.string.base_url)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { 
                    Text(
                        when (provider) {
                            ProviderId.OPENAI -> "https://api.openai.com"
                            ProviderId.GEMINI -> "https://generativelanguage.googleapis.com"
                            ProviderId.ANTHROPIC -> "https://api.anthropic.com"
                        }
                    )
                }
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text(stringResource(R.string.model_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { 
                    Text(
                        when (provider) {
                            ProviderId.OPENAI -> "gpt-3.5-turbo"
                            ProviderId.GEMINI -> "gemini-pro"
                            ProviderId.ANTHROPIC -> "claude-3-sonnet-20240229"
                        }
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newConfig = ProviderConfig(
                            id = provider.id,
                            baseUrl = baseUrl,
                            apiKeyEnc = apiKey,
                            model = model
                        )
                        onSave(newConfig)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && apiKey.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }

                OutlinedButton(
                    onClick = onTest,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && config != null
                ) {
                    Text(stringResource(R.string.test_connection))
                }
            }
        }
    }
}

@Composable
fun WebDAVSettingsSection(
    settings: WebDAVSettings?,
    onSave: (WebDAVSettings) -> Unit,
    onTest: () -> Unit,
    onUpload: () -> Unit,
    onDownload: () -> Unit,
    isLoading: Boolean
) {
    var url by remember(settings) { mutableStateOf(settings?.url ?: "") }
    var username by remember(settings) { mutableStateOf(settings?.username ?: "") }
    var password by remember(settings) { mutableStateOf(settings?.passwordEnc ?: "") }
    var remotePath by remember(settings) { mutableStateOf(settings?.remotePath ?: "/aichat") }
    var includeApiKeys by remember(settings) { mutableStateOf(settings?.includeApiKeys ?: false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.webdav_settings),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.webdav_url)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.username)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = remotePath,
                onValueChange = { remotePath = it },
                label = { Text(stringResource(R.string.remote_path)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("包含 API Keys (不推荐)")
                Switch(
                    checked = includeApiKeys,
                    onCheckedChange = { includeApiKeys = it }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newSettings = WebDAVSettings(
                            url = url,
                            username = username,
                            passwordEnc = password,
                            remotePath = remotePath,
                            includeApiKeys = includeApiKeys
                        )
                        onSave(newSettings)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && url.isNotBlank() && username.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }

                OutlinedButton(
                    onClick = onTest,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && settings != null
                ) {
                    Text(stringResource(R.string.test_connection))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUpload,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && settings != null
                ) {
                    Text(stringResource(R.string.upload))
                }

                Button(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && settings != null
                ) {
                    Text(stringResource(R.string.download))
                }
            }
        }
    }
}
