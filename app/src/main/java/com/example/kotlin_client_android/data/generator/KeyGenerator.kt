package com.example.kotlin_client_android.data.generator

import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper

@Serializable
class KeyGenerator() {


    fun generateIdentityKeyPair(): IdentityKeyPair =
        KeyHelper.generateIdentityKeyPair()

    fun generateRegistrationId(): Int =
        KeyHelper.generateRegistrationId(false)

    fun generateSignedPreKey(identityKeyPair: IdentityKeyPair) =
        KeyHelper.generateSignedPreKey(identityKeyPair, SIGNED_PRE_KEY_ID)

    fun generatePreKeys(): MutableList<PreKeyRecord> =
        KeyHelper.generatePreKeys(PRE_KEY_START, PRE_KEY_COUNT)


    companion object {
        const val PRE_KEY_START = 0
        const val PRE_KEY_COUNT = 10
        const val SIGNED_PRE_KEY_ID = 0
    }
}