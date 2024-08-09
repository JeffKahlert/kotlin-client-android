package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.Message
import com.example.kotlin_client_android.util.Constants
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

    override suspend fun getMessagesByChatId(chatId: String): List<Message> {
        return try{
            client.get("${Constants.BASE_URL}/messages/${chatId}")
                .body<List<Message>>().map { it.toMessage() }
        } catch (ex: Exception) {
            println("ERROR IN GETMESSAGESBYCHATID")
            println("PATH: ${Constants.BASE_URL}/messages/${chatId}")
            ex.printStackTrace()
            emptyList()
        }
    }

}