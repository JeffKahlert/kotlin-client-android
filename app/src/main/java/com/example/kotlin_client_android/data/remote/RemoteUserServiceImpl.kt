package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.RemoteUser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteUserServiceImpl(
    private val client: HttpClient
) : RemoteUserService {
    override suspend fun getAllUsers(): List<RemoteUser> {
        return try {
            client.get("${RemoteUserService.Endpoints.GetAllUsers}").body()
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("ERROR BEIM LADEN DER USER")
            emptyList()
        }
    }

    override suspend fun getUserById(): RemoteUser {
        return RemoteUser("0000", "False", null)
    }
}