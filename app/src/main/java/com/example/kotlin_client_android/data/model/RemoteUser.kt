package com.example.kotlin_client_android.data.model

import com.example.kotlin_client_android.data.model.key.RemoteUserPreKeyBundle
import kotlinx.serialization.Serializable

@Serializable
class RemoteUser(
    val userId: String,
    val userName: String,
    val preKeyBundle: RemoteUserPreKeyBundle
) {

    fun toRemoteUser(): RemoteUser {
        return RemoteUser(
            userId = userId,
            userName = userName,
            preKeyBundle = preKeyBundle
        )
    }
}