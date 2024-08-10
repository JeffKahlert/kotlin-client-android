package com.example.kotlin_client_android.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.kotlin_client_android.data.generator.KeyGenerator
import com.example.kotlin_client_android.data.model.DeviceUser
import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.data.model.key.SerializedPreKey
import com.example.kotlin_client_android.data.remote.RemoteUserService
import com.example.kotlin_client_android.util.Constants
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.whispersystems.libsignal.ecc.ECKeyPair
import org.whispersystems.libsignal.ecc.ECPrivateKey
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.SessionRecord
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi


class UserRepository @Inject constructor(
    private val client: HttpClient,
    private val context: Context,
    private val remoteUserService: RemoteUserService
) {

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USERID = "0"
    }

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun createUser(name: String, deviceId: Int): DeviceUser {
        saveUsername(deviceId)
        return DeviceUser(name, deviceId)
    }

    suspend fun getUsers(): List<RemoteUser> {
        return try {
            remoteUserService.getAllUsers()
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyList()
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun sendPreKeyBundle(user: DeviceUser): Boolean {
        val jsonData = Json.encodeToString(user.getSenderKeyBundle())
        return try {
            val response: HttpResponse = client.post("${Constants.BASE_URL}/keys") {
                contentType(ContentType.Application.Json)
                body = jsonData
            }
            response.status == HttpStatusCode.OK
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }


    private fun saveUsername(deviceId: Int) {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("device_id", deviceId.toString())
            apply()
        }
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERID, null)
    }

    fun getDeviceId(): String? {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("device_id", null)
    }

    fun saveStores(user: DeviceUser) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("Signal_Prefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Get the Session
        val sessionRecord: SessionRecord = user.getSessionStore().loadSession(user.getAddress())
        val serializedSession: ByteArray = sessionRecord.serialize()
        val encode: String = Base64.encodeToString(serializedSession, Base64.DEFAULT)

        editor.putString("session_" + user.getAddress()?.name, encode)


        // Get all PreKeys from PreKeyStore
        for (i in KeyGenerator.PRE_KEY_START
                until KeyGenerator.PRE_KEY_COUNT + KeyGenerator.PRE_KEY_START) {
            val preKey = user.getPreKeyStore().loadPreKey(i)
            val preKeyId = preKey.id
            val preKeyPrivate = preKey.keyPair.privateKey.serialize()
            val preKeyPublic = preKey.keyPair.privateKey.serialize()

            editor.putString(
                "pre_key_private_$preKeyId",
                Base64.encodeToString(preKeyPrivate, Base64.DEFAULT)
            )
            editor.putString(
                "pre_key_public_$preKeyId",
                Base64.encodeToString(preKeyPublic, Base64.DEFAULT)
            )
        }


        // Get signedPreKey from store
        val signedPreKeys = user.getSignedPreKeyStore().loadSignedPreKeys()
        signedPreKeys.forEach{ key ->
            val id: Int = key.id
            val publicKey: ByteArray = key.keyPair.publicKey.serialize()
            val privateKey: ByteArray = key.keyPair.privateKey.serialize()
            val signature: ByteArray = key.signature
            val timeStamp: Long = key.timestamp

            editor.putString(
                "signed_pre_key_public_$id",
                Base64.encodeToString(publicKey, Base64.DEFAULT)
            )
            editor.putString(
                "signed_pre_key_private_$id",
                Base64.encodeToString(privateKey, Base64.DEFAULT)
            )
            editor.putString(
                "signed_pre_key_signature_$id",
                Base64.encodeToString(signature, Base64.DEFAULT)
            )
            editor.putLong(
                "signed_pre_key_timestamp_$id",
                timeStamp
            )

        }

        // Get identityKeys from IdentityStore
        val identityKeyPrivate = user.getIdentityKeyStore().identityKeyPair.privateKey.serialize()
        val identityKeyPublic = user.getIdentityKeyStore().identityKeyPair.publicKey.serialize()
        editor.putString(
            "identity_key_private",
            Base64.encodeToString(identityKeyPrivate, Base64.DEFAULT)
        )
        editor.putString(
            "identity_key_public",
            Base64.encodeToString(identityKeyPublic, Base64.DEFAULT)
        )

        editor.apply()
    }
}