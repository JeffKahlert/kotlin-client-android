package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.data.model.RemoteUserPreKeyBundle
import com.example.kotlin_client_android.util.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemoteUserServiceImpl(
    private val client: HttpClient
) : RemoteUserService {
    override suspend fun getAllUsers(): List<RemoteUser> {
        return try {
            client.get("${Constants.BASE_URL}/user")
                .body<List<RemoteUser>>().map { it.toRemoteUser() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("ENDPOINT ${RemoteUserService.Endpoints.GetAllUsers}")
            println("ERROR BEIM LADEN DER USER: ${ex.message}")
            emptyList()
        }
    }


    override suspend fun getUserById(id: String): Result<RemoteUser> {
        return try {
            val allUsers = client.get("${Constants.BASE_URL}/user")
                .body<List<RemoteUser>>()
                .map { it.toRemoteUser() }

            val user = allUsers.find { it.userId == id }

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("ENDPOINT ${RemoteUserService.Endpoints.GetAllUsers}")
            println("ERROR BEIM LADEN DER USER: ${ex.message}")
            Result.failure(ex)
        }
    }
}