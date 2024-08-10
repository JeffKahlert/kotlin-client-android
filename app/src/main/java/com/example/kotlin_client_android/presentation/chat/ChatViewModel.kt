package com.example.kotlin_client_android.presentation.chat

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_client_android.data.remote.ChatSocketService
import com.example.kotlin_client_android.data.remote.MessageService
import com.example.kotlin_client_android.data.remote.RemoteUserService
import com.example.kotlin_client_android.data.signalstore.SessionController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.PreKeyBundle
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatSocketService: ChatSocketService,
    private val messageService: MessageService,
    private val remoteUserService: RemoteUserService,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionController: SessionController = SessionController(context)


    private val _messageText = mutableStateOf("")
    val messageText: State<String> = _messageText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun connectChat() {
        val chatId = savedStateHandle.get<String>("chatId")
        val userId = savedStateHandle.get<String>("userId")
        initSignalSession(chatId.toString())
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

    private fun initSignalSession(chatId: String) {
        viewModelScope.launch {
            val result = remoteUserService.getUserById(chatId.last().toString())
            result.onSuccess { user ->
                val remotePreKeyBundle = remoteUserService.deserializeFetchedKeys(user.preKeyBundle)
                val remoteSignalAddress =
                    SignalProtocolAddress(user.userName, user.preKeyBundle.deviceId.toInt())
                val sessionBuilder = SessionBuilder(
                    sessionController.loadSessionStore(),
                    sessionController.loadPreKeyStore(),
                    sessionController.loadSignedPreKeyStore(),
                    sessionController.loadIdentityKeyStore(),
                    remoteSignalAddress)
                sessionBuilder.process(remotePreKeyBundle)
            }.onFailure { exception ->
                println("Error fetching user info: ${exception.message}")
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
        val chatId = savedStateHandle.get<String>("chatId")
        if (chatId != null) {
            viewModelScope.launch {
                val result = remoteUserService.getUserById(chatId.last().toString())
                result.onSuccess { user ->
                    val remoteSignalAddress =
                        SignalProtocolAddress(user.userName, user.preKeyBundle.deviceId.toInt())
                    if (messageText.value.isNotBlank()) {
                        val sessionCipher = SessionCipher(
                            sessionController.loadSessionStore(),
                            sessionController.loadPreKeyStore(),
                            sessionController.loadSignedPreKeyStore(),
                            sessionController.loadIdentityKeyStore(),
                            remoteSignalAddress
                        )
                        val cipherText =
                            sessionCipher.encrypt(messageText.value.toByteArray()).toString()
                        chatSocketService.sendMessage(cipherText)
                    }
                }.onFailure { exception ->
                    println("Error : ${exception.message}")
                }
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