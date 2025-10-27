package com.aichat.presentation.conversations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aichat.data.model.Conversation
import com.aichat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as com.aichat.AIChatApplication).chatRepository

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            repository.getAllConversations().collect { conversationList ->
                _conversations.value = conversationList
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
        }
    }

    fun deleteAllConversations() {
        viewModelScope.launch {
            repository.deleteAllConversations()
        }
    }
}
