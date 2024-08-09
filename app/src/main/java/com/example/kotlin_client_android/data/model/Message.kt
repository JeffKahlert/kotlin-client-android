package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Serializable

@Serializable
class Message(
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {

    fun toMessage(): Message {
        return Message(
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            content = content
        )
    }
}