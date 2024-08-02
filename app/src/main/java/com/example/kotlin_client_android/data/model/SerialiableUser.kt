package com.example.kotlin_client_android.data.model

import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.state.PreKeyBundle


class SerializableUser {
    val address: String
    val preKeyBundle: PreKeyBundle

    constructor(address: String, preKeyBundle: PreKeyBundle) {
        this.address = address
        this.preKeyBundle = preKeyBundle
    }
}