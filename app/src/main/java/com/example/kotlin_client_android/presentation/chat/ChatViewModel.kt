package com.example.kotlin_client_android.presentation.chat

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_client_android.data.remote.ChatSocketService
import com.example.kotlin_client_android.data.remote.MessageService
import com.example.kotlin_client_android.data.remote.RemoteUserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatSocketService: ChatSocketService,
    private val messageService: MessageService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun connectChat() {
        val chatId = savedStateHandle.get<String>("chatId")
        val userId = savedStateHandle.get<String>("userId")
        getAllMessages(chatId.toString())
        if (chatId != null && userId != null) {
            viewModelScope.launch {
                val result = chatSocketService.init(chatId, userId)
                result.onSuccess {
                    chatSocketService.incomingMessages()
                        .onEach { message ->
                            val newList = state.value.messages.toMutableList().apply {
                                add(0, message)
                            }
                            _state.value = state.value.copy(
                                messages = newList
                            )
                        }.launchIn(viewModelScope)
                }
                result.onFailure {
                    _toastEvent.emit("ERROR TOAST MESSAGE")
                }
            }
        }
    }

    fun onMessageChange(message: String) {
        _messageText.value = message
    }

    fun disconnect() {
        viewModelScope.launch {
            chatSocketService.closeSession()
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if (messageText.value.isNotBlank()) {
                chatSocketService.sendMessage(messageText.value)
            }
        }
    }

    private fun getAllMessages(chatId: String) {

        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true)
            val result = messageService.getMessagesByChatId(chatId)
            _state.value = state.value.copy(
                messages = result,
                isLoading = false
            )
        }
    }
}