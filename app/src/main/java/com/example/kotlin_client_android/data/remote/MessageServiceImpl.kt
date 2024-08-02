package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.Message
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class MessageServiceImpl(
    private val client: HttpClient
) : MessageService {
    override suspend fun getAllMessages(): List<Message> {
        return try{
            client.get("${MessageService.Endpoints.GetAllMessages}").body()
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyList()
        }
    }
}