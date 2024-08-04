package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Serializable

@Serializable
class RemoteUserPreKeyBundle(
    val userName: String,
    val registrationId: String,
    val identityKey: String,
    val preKeys: List<String>,
    val signedPreKeys: List<String>
)