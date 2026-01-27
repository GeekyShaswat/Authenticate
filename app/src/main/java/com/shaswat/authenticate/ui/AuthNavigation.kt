package com.shaswat.authenticate.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaswat.authenticate.viewmodel.AuthEvent
import com.shaswat.authenticate.viewmodel.AuthState
import com.shaswat.authenticate.viewmodel.AuthViewModel

@Composable
fun AuthNavigation(
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val otpValue = viewModel.otp.collectAsState().value

    when (val state = authState) {
        is AuthState.Initial -> {
            LoginScreen(
                onSendOtp = { email ->
                    viewModel.onEvent(AuthEvent.SendOtp(email))
                },
                isLoading = false
            )
        }

        is AuthState.Loading -> {
            // Show loading on current screen
            when {
                state == AuthState.Loading -> {
                    LoginScreen(
                        onSendOtp = {},
                        isLoading = true
                    )
                }
            }
        }

        is AuthState.OtpSent -> {
            OtpScreen(
                email = state.email,
                generatedAt = state.generatedAt,
                onVerifyOtp = { email, otp ->
                    viewModel.onEvent(AuthEvent.VerifyOtp(email, otp))
                },
                onResendOtp = { email ->
                    viewModel.onEvent(AuthEvent.SendOtp(email))
                },
                getRemainingTime = { email ->
                    viewModel.getRemainingTime(email)
                },
                isLoading = false,
                errorMessage = null,
                authViewModel = viewModel
            )
        }

        is AuthState.OtpError -> {
            OtpScreen(
                email = state.email,
                generatedAt = state.generatedAt,
                onVerifyOtp = { email, otp ->
                    viewModel.onEvent(AuthEvent.VerifyOtp(email, otp))
                },
                onResendOtp = { email ->
                    viewModel.onEvent(AuthEvent.SendOtp(email))
                },
                getRemainingTime = { email ->
                    viewModel.getRemainingTime(email)
                },
                isLoading = false,
                errorMessage = state.message,
                authViewModel = viewModel
            )
        }

        is AuthState.Authenticated -> {
            SessionScreen(
                email = state.email,
                loginTime = state.loginTime,
                onLogout = {
                    viewModel.onEvent(AuthEvent.Logout)
                }
            )
        }
    }
}