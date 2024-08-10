package com.example.kotlin_client_android.data.remote

import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.data.model.key.RemoteUserPreKeyBundle
import com.example.kotlin_client_android.data.model.key.SerializedPreKey
import com.example.kotlin_client_android.data.model.key.SerializedSignedPreKeys
import com.example.kotlin_client_android.util.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.PreKeyBundle
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class RemoteUserServiceImpl(
    private val client: HttpClient
) : RemoteUserService {
    override suspend fun getAllUsers(): List<RemoteUser> {
        return try {
            client.get("${Constants.BASE_URL}/user")
                .body<List<RemoteUser>>().map { it.toRemoteUser() }
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("ENDPOINT ${RemoteUserService.Endpoints.GetAllUsers}")
            println("ERROR BEIM LADEN DER USER: ${ex.message}")
            emptyList()
        }
    }


    override suspend fun getUserById(id: String): Result<RemoteUser> {
        return try {
            val allUsers = client.get("${Constants.BASE_URL}/user")
                .body<List<RemoteUser>>()
                .map { it.toRemoteUser() }

            val user = allUsers.find { it.userId == id }

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("ENDPOINT ${RemoteUserService.Endpoints.GetAllUsers}")
            println("ERROR BEIM LADEN DER USER: ${ex.message}")
            Result.failure(ex)
        }
    }


    override suspend fun deserializeFetchedKeys(fetchedPreKeyBundle: RemoteUserPreKeyBundle): PreKeyBundle {
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



    @OptIn(ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class)
    private fun deserializeSignedPreKeySignature(
        signedPreKeys: Array<SerializedSignedPreKeys>
    ): ByteArray {
        val signatureToByteArray =
            Base64.decode(signedPreKeys[0].signature).toUByteArray().toByteArray()// before signedPreKeys[0].signature.toByteArray()
        return signatureToByteArray
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class)
    private fun deserializeSignedPreKeyPublic(
        signedPreKeys: Array<SerializedSignedPreKeys>
    ): ECPublicKey {
        val signedPublicPreKeyToByteArray =
            Base64.decode(signedPreKeys[0].publicKey).toUByteArray().toByteArray() // before signedPreKeys[0].publicKey.toByteArray()
        val decodeSignedPublicKey: ECPublicKey = Curve.decodePoint(
            signedPublicPreKeyToByteArray, 0
        )
        return decodeSignedPublicKey
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class)
    private fun deserializePublicPreKey(preKeys: Array<SerializedPreKey>): ECPublicKey {
        val basePreKey = Base64.decode(preKeys[0].publicKey).toUByteArray().toByteArray()
        //val preKey = preKeys[0].publicKey.toByteArray(Charsets.UTF_8)
        //println("Public Key ByteArray: ${preKey.joinToString(",")}")
        val decodePublicPreKey: ECPublicKey = Curve.decodePoint(basePreKey, 0)
        return decodePublicPreKey
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalUnsignedTypes::class)
    private fun deserializeIdentityKey(identityKey: String): IdentityKey {
        val identityKeyToByteArray = Base64.decode(identityKey).toUByteArray().toByteArray()// before identityKey.toByteArray()
        return IdentityKey(identityKeyToByteArray,0)
    }


}