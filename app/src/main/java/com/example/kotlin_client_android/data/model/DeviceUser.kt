package com.example.kotlin_client_android.data.model

import com.example.kotlin_client_android.data.generator.KeyGenerator
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.impl.InMemoryIdentityKeyStore
import org.whispersystems.libsignal.state.impl.InMemoryPreKeyStore
import org.whispersystems.libsignal.state.impl.InMemorySessionStore
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import org.whispersystems.libsignal.state.impl.InMemorySignedPreKeyStore
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
class DeviceUser(val name: String,val deviceId: Int) {

    private var registrationId: Int = 0
    private lateinit var store: SignalProtocolStore
    @Contextual private var address: SignalProtocolAddress? = null
    private val generator: KeyGenerator = KeyGenerator()
    @Contextual private lateinit var sessionStore: InMemorySessionStore
    @Contextual private lateinit var preKeyStore: InMemoryPreKeyStore
    @Contextual private lateinit var signedPreKeyStore: InMemorySignedPreKeyStore
    @Contextual private lateinit var identityKeyStore: InMemoryIdentityKeyStore

    init {
        if (address == null) {
            this.address = SignalProtocolAddress(name, deviceId)

            val identityKeyPair = generator.generateIdentityKeyPair()
            registrationId = generator.generateRegistrationId()
            val signedPreKey = generator.generateSignedPreKey(identityKeyPair)
            val preKeys = generator.generatePreKeys()

            sessionStore = InMemorySessionStore()
            preKeyStore = InMemoryPreKeyStore()
            signedPreKeyStore = InMemorySignedPreKeyStore()
            identityKeyStore = InMemoryIdentityKeyStore(
                identityKeyPair,
                registrationId
            )

            this.store = InMemorySignalProtocolStore(identityKeyPair, registrationId)

            runBlocking {
                preKeys.forEach {
                    //store.storePreKey(it.id, it)
                    preKeyStore.storePreKey(it.id, it)
                    println("PREKEY IDs: ${it.id}")
                    println("LOAD PRE KEY WITH ID ${it.id}: ${it.serialize()}")
                }
            }

            runBlocking {
                //store.storeSignedPreKey(signedPreKey.id, signedPreKey)
                signedPreKeyStore.storeSignedPreKey(signedPreKey.id, signedPreKey)
            }

        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun serializeIdentityKeyPublic(): String {
        val identityKey = this.store.identityKeyPair
        return Base64.encode(identityKey.publicKey.serialize())
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun serializeSignedKeyPublic(): List<String> {
        val signedPreKey = this.signedPreKeyStore.loadSignedPreKeys()
        return signedPreKey.map { key ->
            Base64.encode(key.keyPair.publicKey.serialize())
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun serializedPreKeyPublic(): List<String> {
        val preKeys = mutableListOf<String>()
        for (i in KeyGenerator.PRE_KEY_START until KeyGenerator.PRE_KEY_START + KeyGenerator.PRE_KEY_COUNT) {
            try {
                val preKey = this.preKeyStore.loadPreKey(i)
                preKeys.add(Base64.encode(preKey.keyPair.publicKey.serialize()))
            } catch (e: InvalidKeyIdException) {
                // PreKey mit dieser ID existiert nicht, überspringen
                println("PreKey with ID $i does not exist: ${e.message}")
            }
        }
        return preKeys
    }

    fun getSenderKeyBundle() = KeyBundleToSend(
        this.name,
        this.registrationId,
        serializeIdentityKeyPublic(),
        serializedPreKeyPublic(),
        serializeSignedKeyPublic(),
    )
}