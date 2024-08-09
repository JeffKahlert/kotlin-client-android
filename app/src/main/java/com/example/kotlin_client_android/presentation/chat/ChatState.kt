package com.example.kotlin_client_android.presentation.chat

import com.example.kotlin_client_android.data.model.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)
