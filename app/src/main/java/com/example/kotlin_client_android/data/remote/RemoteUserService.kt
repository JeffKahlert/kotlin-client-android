package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.User
import com.example.kotlin_client_android.util.Constants

interface RemoteUserService {

    suspend fun getAllUsers(): List<User>

    suspend fun getUserById(): User

    sealed class Endpoints(val url: String) {
        object GetAllUsers: Endpoints("${Constants.BASE_URL}/users")
    }
}