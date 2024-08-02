package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteUserServiceImpl(
    private val client: HttpClient
) : RemoteUserService {
    override suspend fun getAllUsers(): List<User> {
        return try {
            client.get("${RemoteUserService.Endpoints.GetAllUsers}").body()
        } catch (ex: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserById(): User {
        TODO("Not yet implemented")
    }
}