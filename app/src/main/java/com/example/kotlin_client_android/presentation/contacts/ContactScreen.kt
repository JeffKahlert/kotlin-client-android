package com.example.kotlin_client_android.presentation.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kotlin_client_android.data.model.RemoteUser
import com.example.kotlin_client_android.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    onClickSuccess: (String, String, String) -> Unit // Übergibt chatId und userId
) {
    val users by viewModel.remoteUsers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple40,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(users) { user ->
                UserCard(user = user, onClick = {
                    val deviceId = viewModel.getDeviceId()
                    val chatId = "$deviceId${user.userId}"
                    onClickSuccess(chatId, deviceId.toString() , user.userId)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
@Composable
fun UserCard(user: RemoteUser, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.userName, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(user.userId, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
