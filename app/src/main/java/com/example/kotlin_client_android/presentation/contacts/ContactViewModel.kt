package com.example.kotlin_client_android.presentation.contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.data.remote.RemoteUserService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor (
  private val remoteUserService: RemoteUserService,
  @ApplicationContext private val context: Context
) :ViewModel() {

    private val _remoteUsers = MutableStateFlow<List<RemoteUser>>(emptyList())
    val remoteUsers: StateFlow<List<RemoteUser>> get() = _remoteUsers

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _remoteUsers.value = remoteUserService.getAllUsers()
        }
    }

    fun getDeviceId(): String? {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        println(sharedPref.getString("device_id", null))
        return sharedPref.getString("device_id", null)
    }

    fun printUserInfo(userId: String) {
        viewModelScope.launch {
            val result = remoteUserService.getUserById(userId)
            result.onSuccess { user ->
                println("User Info: ${user.userId}")
            }.onFailure { exception ->
                println("Error fetching user info: ${exception.message}")
            }
        }
    }
}