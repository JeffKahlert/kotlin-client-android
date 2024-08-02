package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.util.Constants
import org.whispersystems.libsignal.state.PreKeyBundle

interface KeyService {

    suspend fun getKeysByUserId(): List<PreKeyBundle>

    sealed class Endpoints(val url: String) {
        object GetKeysByUserId: Endpoints("${Constants.BASE_URL}/keys")
    }
}