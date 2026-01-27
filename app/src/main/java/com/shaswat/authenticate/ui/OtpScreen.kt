package com.shaswat.authenticate.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaswat.authenticate.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    email: String,
    generatedAt: Long,
    onVerifyOtp: (String, String) -> Unit,
    onResendOtp: (String) -> Unit,
    getRemainingTime: (String) -> Long,
    isLoading: Boolean,
    authViewModel: AuthViewModel,
    errorMessage: String?
) {
    var otp by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var timeRemaining by remember { mutableStateOf(60L) }
    var otpKey by remember { mutableStateOf(0) }
    val otpValue = authViewModel.otp.collectAsState().value
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Countdown timer effect
    LaunchedEffect(email,generatedAt) {
        timeRemaining = getRemainingTime(email)
        while (timeRemaining > 0) {
            delay(1000L) // Wait 1 second
            timeRemaining = getRemainingTime(email)
        }
    }
    LaunchedEffect(otpKey) {
        Toast.makeText(context, "OTP: $otpValue ", Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "We've sent a 6-digit code to",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Timer Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (timeRemaining > 10) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Time Remaining",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${timeRemaining}s",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (timeRemaining > 10) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        // OTP Input Field
        OutlinedTextField(
            value = otp,
            onValueChange = {
                if (it.text.length <= 6 && it.text.all { char -> char.isDigit() }) {
                    otp = it
                }
            },
            label = { Text("Enter 6-digit OTP") },
            placeholder = { Text("000000") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading && timeRemaining > 0,
            isError = errorMessage != null
        )

        // Error Message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Verify Button
        Button(
            onClick = {
                if (otp.text.length == 6) {
                    onVerifyOtp(email, otp.text)
                }
                if (errorMessage != null){
                    otp = TextFieldValue("")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && otp.text.length == 6 && timeRemaining > 0
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Verify OTP")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend OTP Button
        TextButton(
            onClick = {
                otp = TextFieldValue("")
                onResendOtp(email)

            },
            enabled = !isLoading
        ) {
            Text(
                text = if (timeRemaining > 0) {
                    "Didn't receive? Resend OTP"
                } else {
                    "OTP Expired - Resend"
                }
            )
        }
    }
}