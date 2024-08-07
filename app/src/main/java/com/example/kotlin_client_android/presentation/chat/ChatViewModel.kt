package com.example.kotlin_client_android.presentation.chat

import androidx.lifecycle.ViewModel
import com.example.kotlin_client_android.data.remote.RemoteUserService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val remoteUserService: RemoteUserService
): ViewModel() {

}