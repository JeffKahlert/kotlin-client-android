package com.example.kotlin_client_android.data.signalstore

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.kotlin_client_android.data.generator.KeyGenerator
import com.example.kotlin_client_android.data.model.DeviceUser
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECKeyPair
import org.whispersystems.libsignal.ecc.ECPrivateKey
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SessionRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.impl.InMemoryIdentityKeyStore
import org.whispersystems.libsignal.state.impl.InMemoryPreKeyStore
import org.whispersystems.libsignal.state.impl.InMemorySessionStore
import org.whispersystems.libsignal.state.impl.InMemorySignedPreKeyStore

class SessionController(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("Signal_Prefs", Context.MODE_PRIVATE)

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)


    // Laden eines SessionStore aus SharedPreferences
    fun loadSessionStore(): InMemorySessionStore? {
        val sessionStore = InMemorySessionStore()
        val deviceUserSignalProtocolAddress: SignalProtocolAddress

        val deviceId = sharedPref.getString("device_id", null)?.toInt()
        val username = sharedPreferences.getString("username", null)
        val encodedSession = sharedPreferences.getString("session_$username", null)

        if (deviceId == null) return null

        deviceUserSignalProtocolAddress = SignalProtocolAddress(username, deviceId)

        if (encodedSession != null) {
            val serializedSession = Base64.decode(encodedSession, Base64.DEFAULT)
            val sessionRecord = SessionRecord(serializedSession)
            sessionStore.storeSession(deviceUserSignalProtocolAddress, sessionRecord)
        }

        return sessionStore
    }

    // Laden des PreKeyStore aus SharedPreferences
    @OptIn(ExperimentalUnsignedTypes::class)
    fun loadPreKeyStore(): InMemoryPreKeyStore {
        val preKeyStore = InMemoryPreKeyStore()

        for (i in KeyGenerator.PRE_KEY_START until KeyGenerator.PRE_KEY_COUNT + KeyGenerator.PRE_KEY_START) {
            val encodedPrivate = sharedPreferences.getString("pre_key_private_$i", null)
            val encodedPublic = sharedPreferences.getString("pre_key_public_$i", null)


            println("ENCODED PRIVATE KEY IN PREKEYSTORE: $encodedPrivate")
            println("ENCODED PUBLIC KEY IN PREKEYSTORE: $encodedPublic")

            if (encodedPrivate != null && encodedPublic != null) {
                val basePublicKey =
                    Base64.decode(encodedPublic, Base64.DEFAULT).toUByteArray().toByteArray()
                val basePrivateKey =
                    Base64.decode(encodedPrivate, Base64.DEFAULT).toUByteArray().toByteArray()

                val privateKey = Curve.decodePrivatePoint(basePrivateKey)
                val publicKey = Curve.decodePoint(basePublicKey, 0)
                val keyPair = ECKeyPair(publicKey, privateKey)

                val preKeyRecord = PreKeyRecord(i, keyPair)
                preKeyStore.storePreKey(i, preKeyRecord)
            }
        }

        return preKeyStore
    }

    // Laden des SignedPreKeyStore aus SharedPreferences
    @OptIn(ExperimentalUnsignedTypes::class)
    fun loadSignedPreKeyStore(): InMemorySignedPreKeyStore {
        val signedPreKeyStore = InMemorySignedPreKeyStore()

        for (i in 0..1) {
            val encodedPublic = sharedPreferences.getString("signed_pre_key_public_$i", null)
            val encodedPrivate = sharedPreferences.getString("signed_pre_key_private_$i", null)
            val encodedSignature = sharedPreferences.getString("signed_pre_key_signature_$i", null)
            val timestamp = sharedPreferences.getLong("signed_pre_key_timestamp_$i", 0)

            if (encodedPublic != null && encodedPrivate != null && encodedSignature != null) {

                val basePublicKey =
                    Base64.decode(encodedPublic, Base64.DEFAULT).toUByteArray().toByteArray()
                val basePrivateKey =
                    Base64.decode(encodedPrivate, Base64.DEFAULT).toUByteArray().toByteArray()
                val baseSignature =
                    Base64.decode(encodedSignature, Base64.DEFAULT).toUByteArray().toByteArray()

                val publicKey: ECPublicKey = Curve.decodePoint(basePublicKey, 0)
                val privateKey: ECPrivateKey = Curve.decodePrivatePoint(basePrivateKey)
                val keyPair = ECKeyPair(publicKey, privateKey)

                val signedPreKeyRecord = SignedPreKeyRecord(i, timestamp, keyPair, baseSignature)
                signedPreKeyStore.storeSignedPreKey(i, signedPreKeyRecord)
            }
        }

        return signedPreKeyStore
    }

    // Laden des IdentityKeyStore aus SharedPreferences
    @OptIn(ExperimentalUnsignedTypes::class)
    fun loadIdentityKeyStore(): InMemoryIdentityKeyStore? {
        val identityKeyStore: InMemoryIdentityKeyStore


        val encodedPrivate = sharedPreferences.getString("identity_key_private", null)
        val encodedPublic = sharedPreferences.getString("identity_key_public", null)
        val encodeRegistrationId = sharedPreferences.getInt("registration_id", 0)

        if (encodedPrivate != null && encodedPublic != null && encodeRegistrationId != 0) {
            val basePublicKey =
                Base64.decode(encodedPublic, Base64.DEFAULT).toUByteArray().toByteArray()
            val basePrivateKey =
                Base64.decode(encodedPrivate, Base64.DEFAULT).toUByteArray().toByteArray()


            val privateKey = Curve.decodePrivatePoint(basePrivateKey)
            val publicKeyECPublic =
                Curve.decodePoint(basePublicKey, 0)
            val publicKey = IdentityKey(publicKeyECPublic)
            val identityKeyPair = IdentityKeyPair(publicKey, privateKey)
            val registrationId: Int  = encodeRegistrationId

            identityKeyStore = InMemoryIdentityKeyStore(identityKeyPair, registrationId)

            return identityKeyStore

        } else {

            println("ERROR IN loadIdentityKeyStore()")
            return null
        }
    }

}
