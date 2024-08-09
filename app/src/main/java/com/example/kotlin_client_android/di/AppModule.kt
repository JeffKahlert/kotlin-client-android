package com.example.kotlin_client_android.di

import android.content.Context
import com.example.kotlin_client_android.data.remote.ChatSocketImpl
import com.example.kotlin_client_android.data.remote.ChatSocketService
import com.example.kotlin_client_android.data.remote.MessageService
import com.example.kotlin_client_android.data.remote.MessageServiceImpl
import com.example.kotlin_client_android.data.remote.RemoteUserService
import com.example.kotlin_client_android.data.remote.RemoteUserServiceImpl
import com.example.kotlin_client_android.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(DefaultRequest)
            install(Logging)
            install(WebSockets)
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging)
        }
    }
    @Provides
    @Singleton
    fun provideRemoteUserService(client: HttpClient): RemoteUserService {
        return RemoteUserServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        client: HttpClient,
        @ApplicationContext context: Context,
        remoteUserService: RemoteUserService
    ): UserRepository {
        return UserRepository(client, context, remoteUserService)
    }


    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Provides
    @Singleton
    fun provideChatSocketService(client: HttpClient): ChatSocketService {
        return ChatSocketImpl(client)
    }

    @Provides
    @Singleton
    fun provideMessageService(client: HttpClient): MessageService {
        return MessageServiceImpl(client)
    }
}