package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.Message
import com.example.kotlin_client_android.util.Constants

interface MessageService {

    suspend fun getAllMessages(): List<Message>

    suspend fun getMessagesByChatId(chatId: String): List<Message>

    sealed class Endpoints(val url: String) {
        object GetAllMessages: Endpoints("${Constants.BASE_URL}/messages")
    }
}