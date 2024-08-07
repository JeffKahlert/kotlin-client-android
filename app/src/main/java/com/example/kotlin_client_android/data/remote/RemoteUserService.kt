package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.util.Constants

interface RemoteUserService {

    suspend fun getAllUsers(): List<RemoteUser>

    suspend fun getUserById(id: String): Result<RemoteUser>

    sealed class Endpoints(val url: String) {
        object GetAllUsers: Endpoints("${Constants.BASE_URL}/user")
    }
}