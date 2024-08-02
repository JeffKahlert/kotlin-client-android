package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Serializable


class Message(
    val content: String,
    val userId: String,
) {
}