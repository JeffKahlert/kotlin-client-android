package com.example.kotlin_client_android.presentation.chat

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlin_client_android.data.model.Message
import com.example.kotlin_client_android.data.remote.ChatSocketService
import com.example.kotlin_client_android.data.remote.MessageService
import com.example.kotlin_client_android.data.remote.RemoteUserService
import com.example.kotlin_client_android.data.signalstore.SessionController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.impl.InMemorySessionStore
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

    private lateinit var sessionCipher: SessionCipher

    fun connectChat() {
        val chatId = savedStateHandle.get<String>("chatId")
        val remoteUserId = savedStateHandle.get<String>("userId")

        if (chatId != null && remoteUserId != null) {
            viewModelScope.launch {
                try {
                    val result = chatSocketService.init(chatId, remoteUserId)
                    result.onSuccess {
                        chatSocketService.incomingMessages()
                            .onEach { message ->
                                try {
                                    handleIncomingMessage(message)
                                } catch (e: CancellationException) {
                                    println("Message handling cancelled: ${e.message}")
                                } catch (e: Exception) {
                                    println("Error handling message: ${e.message}")
                                }
                            }
                            .launchIn(viewModelScope)
                    }
                    result.onFailure {
                        _toastEvent.emit("ERROR TOAST MESSAGE")
                    }
                } catch (e: CancellationException) {
                    println("Chat connection cancelled: ${e.message}")
                } catch (e: Exception) {
                    println("Error connecting to chat: ${e.message}")
                }
            }
        }
    }


    private suspend fun initSignalSession(chatId: String) {
        println("Chat ID: ${chatId}")
        val result = remoteUserService.getUserById(chatId.last().toString())
        result.onSuccess { user ->
            val remotePreKeyBundle = remoteUserService.deserializeFetchedKeys(user.preKeyBundle)
            val sessionStore = InMemorySessionStore()
            val remoteSignalAddress = SignalProtocolAddress(user.userName, user.preKeyBundle.deviceId.toInt())

            val sessionBuilder = SessionBuilder(
                sessionStore,
                sessionController.loadPreKeyStore(),
                sessionController.loadSignedPreKeyStore(),
                sessionController.loadIdentityKeyStore(),
                remoteSignalAddress
            )

            sessionBuilder.process(remotePreKeyBundle)

            sessionCipher = SessionCipher(
                sessionStore,
                sessionController.loadPreKeyStore(),
                sessionController.loadSignedPreKeyStore(),
                sessionController.loadIdentityKeyStore(),
                remoteSignalAddress
            )
        }.onFailure { exception ->
            println("Error fetching user info: ${exception.message}")
        }
    }

    private suspend fun handleIncomingMessage(message: Message) {
        if (savedStateHandle.get<String>("chatId").equals(message.chatId)) {
            initSignalSession(message.chatId.last().toString())
        } else {
            initSignalSession(message.chatId.first().toString())
        }

        withContext(Dispatchers.Main) {
            val newList = state.value.messages.toMutableList().apply {
                add(0, message)
            }
            _state.value = state.value.copy(messages = newList)
        }

        withContext(Dispatchers.IO) {
            try {
                val messageToByteArray = message.content.decodeBase64Bytes()

                // Überprüfe, ob eine gültige Session existiert
                val senderAddress = SignalProtocolAddress(message.senderId, message.receiverId.toInt())
                val sessionExists = sessionController.loadSessionStore()?.containsSession(senderAddress) ?: false

                //if (sessionExists) {
                    val decryptedMessage = sessionCipher.decrypt(PreKeySignalMessage(messageToByteArray))
                    val messageText = String(decryptedMessage)

                    withContext(Dispatchers.Main) {
                        val newMessage = Message(
                            chatId = message.chatId,
                            senderId = message.senderId,
                            receiverId = message.receiverId,
                            content = messageText
                        )
                        val newList = state.value.messages.toMutableList().apply {
                            add(0, newMessage)
                        }
                        _state.value = state.value.copy(messages = newList)
                    }
                //} else {
                    println("No valid session found for sender ${message.senderId}.")
                    // Optional: Session resynchronisieren
                    // initSignalSession(message.chatId)
                    // handleIncomingMessage(message)
                //}
            } catch (e: InvalidMessageException) {
                e.printStackTrace()
                println("Fehler bei der Entschlüsselung: ${e.message}")
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

    @OptIn(ExperimentalEncodingApi::class)
    fun sendMessage() {
        val chatId = savedStateHandle.get<String>("chatId")
        if (chatId != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    initSignalSession(chatId.toString())
                    val result = remoteUserService.getUserById(chatId.last().toString())
                    result.onSuccess { user ->
                        val remoteSignalAddress =
                            SignalProtocolAddress(user.userName, user.preKeyBundle.deviceId.toInt())

                        if (messageText.value.isNotBlank()) {
                            try {
                                val message = messageText.value.toByteArray()
                                val cipherText = sessionCipher.encrypt(message)
                                val preKeySignalMessage = PreKeySignalMessage(cipherText.serialize())
                                chatSocketService.sendMessage(Base64.encode(preKeySignalMessage.serialize()))
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                println("ERROR BEI SENDMESSAGE CIPHERTEXT: ${ex.message}")
                            }
                        }
                    }.onFailure { exception ->
                        println("Error : ${exception.message}")
                    }
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
