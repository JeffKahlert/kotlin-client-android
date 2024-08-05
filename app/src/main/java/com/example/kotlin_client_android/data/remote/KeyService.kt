package com.example.kotlin_client_android.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlin_client_android.data.model.RemoteUserPreKeyBundle
import com.example.kotlin_client_android.data.model.key.SerializedPreKey
import com.example.kotlin_client_android.data.model.key.SerializedSignedPreKeys
import com.example.kotlin_client_android.util.Constants
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.PreKeyBundle
import java.util.Base64

interface KeyService {

    suspend fun getKeysByUserId(): List<PreKeyBundle>

    sealed class Endpoints(val url: String) {
        object GetKeysByUserId: Endpoints("${Constants.BASE_URL}/keys")
    }


    suspend fun serializeFetchedKeys(fetchedPreKeyBundle: RemoteUserPreKeyBundle): PreKeyBundle {
        return PreKeyBundle(
            fetchedPreKeyBundle.registrationId.toInt(),
            fetchedPreKeyBundle.deviceId.toInt(),
            fetchedPreKeyBundle.preKeys[0].id.toInt(),
            deserializePublicPreKey(fetchedPreKeyBundle.preKeys),
            fetchedPreKeyBundle.signedPreKeys[0].id.toInt(),
            deserializeSignedPreKeyPublic(fetchedPreKeyBundle.signedPreKeys),
            deserializeSignedPreKeySignature(fetchedPreKeyBundle.signedPreKeys),
            deserializeIdentityKey(fetchedPreKeyBundle.identityKey)
        )
    }



    private fun deserializeSignedPreKeySignature(
        signedPreKeys: List<SerializedSignedPreKeys>
    ): ByteArray {
        val signatureToByteArray = signedPreKeys[0].signature.toByteArray()
        return signatureToByteArray
    }

    private fun deserializeSignedPreKeyPublic(
        signedPreKeys: List<SerializedSignedPreKeys>
    ): ECPublicKey {
        val signedPublicPreKeyToByteArray = signedPreKeys[0].publicKey.toByteArray()
        val decodeSignedPublicKey: ECPublicKey = Curve.decodePoint(
            signedPublicPreKeyToByteArray, 0
        )
        return decodeSignedPublicKey
    }

    private fun deserializePublicPreKey(preKeys: List<SerializedPreKey>): ECPublicKey {
        val preKey = preKeys[0].publicKey.toByteArray()
        val decodePublicPreKey: ECPublicKey = Curve.decodePoint(preKey, 0)
        return decodePublicPreKey
    }

    private fun deserializeIdentityKey(identityKey: String): IdentityKey {
        val identityKeyToByteArray = identityKey.toByteArray()
        return IdentityKey(identityKeyToByteArray,0)
    }


}