package com.example.kotlin_client_android.data.model.key

import kotlinx.serialization.Serializable

@Serializable
class RemoteUserPreKeyBundle(
    val userName: String,
    val deviceId: String,
    val registrationId: String,
    val identityKey: String,
    val preKeys: Array<SerializedPreKey>,
    val signedPreKeys: Array<SerializedSignedPreKeys>
)