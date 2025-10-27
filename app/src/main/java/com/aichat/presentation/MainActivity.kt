package com.aichat.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aichat.R
import com.aichat.presentation.chat.ChatScreen
import com.aichat.presentation.conversations.ConversationsScreen
import com.aichat.presentation.settings.SettingsScreen
import com.aichat.presentation.theme.AIChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIChatTheme {
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                                label = { Text(stringResource(R.string.chat_tab)) },
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = null) },
                                label = { Text(stringResource(R.string.conversations_tab)) },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text(stringResource(R.string.settings_tab)) },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                    }
                ) { paddingValues ->
                    when (selectedTab) {
                        0 -> ChatScreen(Modifier.padding(paddingValues))
                        1 -> ConversationsScreen(Modifier.padding(paddingValues))
                        2 -> SettingsScreen(Modifier.padding(paddingValues))
                    }
                }
            }
        }
    }
}
