package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.state.PreKeyBundle

@Serializable
class RemoteUser(
    //@SerialName("_id") val id: String,
    val userId: String,
    val userName: String,
    val preKeyBundle: RemoteUserPreKeyBundle
) {

    fun toRemoteUser(): RemoteUser {
        return RemoteUser(
            //id = id,
            userId = userId,
            userName = userName,
            preKeyBundle = preKeyBundle
        )
    }
}