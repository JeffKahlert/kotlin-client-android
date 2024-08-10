package com.example.kotlin_client_android.presentation.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_client_android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: HttpClient,
    private val savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val USERNAME_KEY = "username"
    }

    var username: String
        get() = (savedStateHandle.get(USERNAME_KEY) ?: userRepository.getUsername()).toString()
        set(value) {
            savedStateHandle[USERNAME_KEY] = value
        }

    fun register(username: String, deviceId: Int) {
        this.username = username
        viewModelScope.launch {
            val user = userRepository.createUser(username, deviceId)
            val success = userRepository.sendPreKeyBundle(user)
            if (success) {
                userRepository.saveStores(user)
                println("Keys auf den Server geladen")
                println("Alle Session Infos in SharedPreferences gespeichert")
            } else {
                println("Fehler bei Schlüsselübertragung")
            }
        }
    }
}

