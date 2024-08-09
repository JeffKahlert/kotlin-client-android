package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.Message
import com.example.kotlin_client_android.util.Constants
import kotlinx.coroutines.flow.Flow

interface ChatSocketService {

    suspend fun init(
        chatId: String,
        userId: String
    ): Result<Unit>

    suspend fun sendMessage(message: String)

    suspend fun incomingMessages(): Flow<Message>

    suspend fun closeSession()

    sealed class Endpoint(val url: String) {
        object ChatSocket: Endpoint("${Constants.WEBSOCKET_BASE_UR}/chat")
    }
}