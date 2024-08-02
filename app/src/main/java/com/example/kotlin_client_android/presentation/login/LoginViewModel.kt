package com.example.kotlin_client_android.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_client_android.data.model.SerializableDataTest
import com.example.kotlin_client_android.data.model.User
import com.example.kotlin_client_android.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.Identity.encode
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: HttpClient,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val USERNAME_KEY = "username"
    }

    var username: String
        get() = savedStateHandle.get(USERNAME_KEY) ?: ""
        set(value) {
            savedStateHandle[USERNAME_KEY] = value
        }

    fun login(username: String) {
        this.username = username  // Speichern des Benutzernamens im SavedStateHandle
        viewModelScope.launch {
            val user = User(username)
            val serializableUser = user.toSerializableUser()
            val preKeyBundle = "123test123"
            val preKeys: List<String> = arrayOf("Key1", "Key2").toList()
            val signedPreKeys = arrayOf("Key").toList()


            val data = SerializableDataTest(
                userName = username,
                registrationId = "registrationIdTest",
                identityKey = "IdentityKeyTest",
                preKeys = preKeys,
                signedPreKeys = signedPreKeys
            )
            /*val data = mapOf(
                "userName" to username,
                "registrationId" to "registrationIdTest",
                "identityKey" to "IdentityKeyTest",
                "preKeys" to preKeys,
                "signedPreKeys" to signedPreKeys
            )*/

            val jsonData = Json.encodeToString(data)

            try {
                val response: HttpResponse = client.post("${Constants.BASE_URL}/keys") {
                    contentType(ContentType.Application.Json)
                    setBody(jsonData)
                }

                if (response.status == HttpStatusCode.OK) {
                    println("Keys auf den Server geladen")
                } else {
                    println("Fehler bei Schlüsselübertragung")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
