package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Serializable


class Message(
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
}