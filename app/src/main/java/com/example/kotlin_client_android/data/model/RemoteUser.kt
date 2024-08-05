package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.state.PreKeyBundle

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