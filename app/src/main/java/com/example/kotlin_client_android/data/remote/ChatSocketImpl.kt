package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.Message
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class ChatSocketImpl(
    private val client: HttpClient
): ChatSocketService {

    private var socket: WebSocketSession? = null

    override suspend fun init(chatId: String, userId: String): Result<Unit> {
        return try {
            socket = client.webSocketSession {
                url("${ChatSocketService.Endpoint.ChatSocket.url}/${chatId}/${userId}")
            }
            if (socket?.isActive == true) {
                Result.success(Unit)
            } else Result.failure(Exception("Keine Session aktiv"))
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.failure(Exception("Unkown Error"))
        }

    }

    override suspend fun sendMessage(message: String) {
        try {
            socket?.send(Frame.Text(message))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override suspend fun incomingMessages(): Flow<Message> {
        return try {
            socket?.incoming?.receiveAsFlow()
                ?.filter { it is Frame.Text }
                ?.map {
                    val json = (it as? Frame.Text)?.readText() ?: ""
                    val message = Json.decodeFromString<Message>(json)
                    message.toMessage()
                } ?: flow {}
            } catch (ex: Exception) {
                ex.printStackTrace()
                flow { }
        }
    }

    override suspend fun closeSession() {
        socket?.close()
    }
}