package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.data.model.key.RemoteUserPreKeyBundle
import com.example.kotlin_client_android.util.Constants
import org.whispersystems.libsignal.state.PreKeyBundle

interface RemoteUserService {

    suspend fun getAllUsers(): List<RemoteUser>

    suspend fun getUserById(id: String): Result<RemoteUser>

    suspend fun deserializeFetchedKeys(fetchedPreKeyBundle: RemoteUserPreKeyBundle): PreKeyBundle

    sealed class Endpoints(val url: String) {
        object GetAllUsers: Endpoints("${Constants.BASE_URL}/user")
    }
}