package com.example.kotlin_client_android.data.model.key

import kotlinx.serialization.Serializable

@Serializable
class KeyBundleToSend(
    val userName: String,
    val deviceId: Int,
    val registrationId: Int,
    val identityKey: String,
    val preKeys: List<SerializedPreKey>,
    val signedPreKeys: List<SerializedSignedPreKeys>
) {


}