package com.example.kotlin_client_android.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kotlin_client_android.data.model.DeviceUser
import com.example.kotlin_client_android.data.model.RemoteUser
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
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val client: HttpClient,
    private val context: Context,
    private val remoteUserService: RemoteUserService
) {

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USERNAME = "username"
    }

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun createUser(name: String, deviceId: Int): DeviceUser {
        //saveUsername(name)
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


    private fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
}