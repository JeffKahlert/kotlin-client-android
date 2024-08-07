package com.example.kotlin_client_android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlin_client_android.presentation.chat.ChatScreen
import com.example.kotlin_client_android.presentation.contacts.ContactScreen
import com.example.kotlin_client_android.presentation.login.LoginScreen
import com.example.kotlin_client_android.ui.theme.KotlinclientandroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val deviceId = sharedPref.getString("device_id", null)
        setContent {
            KotlinclientandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    LandingPage(deviceId)
                }
            }
        }
    }
}

@Composable
fun LandingPage(deviceId: String?) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = if (deviceId == null) "login_screen" else "contact_screen") {
        composable("login_screen") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("contact_screen")
            })
        }
        composable("contact_screen"){
            ContactScreen(onClickSuccess = {
                navController.navigate("chat_screen")
            })
        }
        composable("chat_screen"){
            ChatScreen()
        }
    }
}
