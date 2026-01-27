package com.shaswat.authenticate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionScreen(
    email: String,
    loginTime: Long,
    onLogout: () -> Unit
) {
    var sessionDuration by remember { mutableStateOf(0L) }
    var scrollState = rememberScrollState()

    // Live timer effect
    LaunchedEffect(loginTime) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            sessionDuration = (currentTime - loginTime) / 1000 // Convert to seconds
            delay(1000L) // Update every second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = "Session",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Session Active",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Logged in as",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Session Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Session Started At",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = formatTimestamp(loginTime),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Session Duration",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = formatDuration(sessionDuration),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Logout")
        }
    }
}

/**
 * Format timestamp to readable time
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Format duration in seconds to mm:ss
 */
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}