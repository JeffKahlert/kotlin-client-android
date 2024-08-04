package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Serializable

@Serializable
class KeyBundleToSend(
    val userName: String,
    val registrationId: Int,
    val identityKey: String,
    val preKeys: List<String>,
    val signedPreKeys: List<String>
) {


}