package com.example.kotlin_client_android.data.model.key

import kotlinx.serialization.Serializable

@Serializable
class SerializedPreKey(
    val id: String,
    val publicKey: String
) {
}