package com.example.kotlin_client_android.data.model

import com.example.kotlin_client_android.data.model.key.SerializedPreKey
import com.example.kotlin_client_android.data.model.key.SerializedSignedPreKeys
import kotlinx.serialization.Serializable

@Serializable
class RemoteUserPreKeyBundle(
    val userName: String,
    val deviceId: String,
    val registrationId: String,
    val identityKey: String,
    val preKeys: List<SerializedPreKey>,
    val signedPreKeys: List<SerializedSignedPreKeys>
)