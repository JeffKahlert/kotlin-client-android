package com.example.kotlin_client_android.data.model

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyStore
import org.whispersystems.libsignal.state.SessionStore
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.impl.InMemoryIdentityKeyStore
import org.whispersystems.libsignal.state.impl.InMemoryPreKeyStore
import org.whispersystems.libsignal.state.impl.InMemorySessionStore
import org.whispersystems.libsignal.util.KeyHelper
import kotlinx.coroutines.runBlocking
import org.whispersystems.libsignal.state.SignedPreKeyStore
import org.whispersystems.libsignal.state.impl.InMemorySignedPreKeyStore


class User(
    address: String
) {
    private val testAdresse = address
    private lateinit var store: SignalProtocolStore
    private lateinit var preKeyBundle: PreKeyBundle
    private lateinit var address: SignalProtocolAddress

    init {

            this.address = SignalProtocolAddress(address, 1)
            var deviceId: Int = 1
            val identityKeyPair: IdentityKeyPair? = KeyHelper.generateIdentityKeyPair()
            val registrationId = KeyHelper.generateRegistrationId(false)
            var preKeys = KeyHelper.generatePreKeys(0, 10)
            var signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 0)

            var sessionStore: SessionStore = InMemorySessionStore()
            var preKeyStore: PreKeyStore = InMemoryPreKeyStore()
            var signedPreKeyStore: SignedPreKeyStore = InMemorySignedPreKeyStore()
            var identityStore = InMemoryIdentityKeyStore(identityKeyPair, registrationId)

            runBlocking {
                for (p in preKeys) {
                    preKeyStore.storePreKey(p.id, p)
                }
            }
            runBlocking {
                signedPreKeyStore.storeSignedPreKey(signedPreKey.id, signedPreKey)
            }

            var preKeyPublic = preKeys[0].keyPair.publicKey

            this.preKeyBundle = PreKeyBundle(
                registrationId,
                deviceId,
                1,
                preKeyPublic,
                signedPreKey.id,
                signedPreKey.keyPair.publicKey,
                signedPreKey.signature,
                identityKeyPair?.publicKey
            )

    }

    fun getPreKeyBundle(): PreKeyBundle = this.preKeyBundle

    fun toSerializableUser(): SerializableUser {
        return SerializableUser(this.testAdresse, preKeyBundle)
    }
}