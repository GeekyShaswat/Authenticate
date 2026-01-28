package com.shaswat.authenticate.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaswat.authenticate.analytics.AnalyticsLogger
import com.shaswat.authenticate.data.OtpManager
import com.shaswat.authenticate.data.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val otpManager = OtpManager()
    private val analyticsLogger = AnalyticsLogger(application)

    // State that UI observes
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _otp = MutableStateFlow<String>("")
    val otp : StateFlow<String> = _otp

    // Current email being processed
    private var currentEmail: String = ""
    private var generatedAt: Long = 0L

    /**
     * Handles all authentication events from UI
     */
    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SendOtp -> sendOtp(event.email)
            is AuthEvent.VerifyOtp -> verifyOtp(event.email, event.otp)
            is AuthEvent.Logout -> logout()
            is AuthEvent.ClearError -> clearError()
        }
    }

    /**
     * Sends OTP to the given email
     */
    private fun sendOtp(email: String) {
        Log.d("TAG"," send otp clicked ")
        viewModelScope.launch {
            if (!isValidEmail(email)) {
                _authState.value = AuthState.OtpError("Please enter a valid email", email, System.currentTimeMillis())
                return@launch
            }

            _authState.value = AuthState.Loading

            // Generate OTP
            val otpCode = otpManager.generateOtp(email)
            currentEmail = email
            generatedAt = System.currentTimeMillis()

            // Log to Firebase
            analyticsLogger.logOtpGenerated(email)

            // In real app, you'd send this via SMS/Email
            // For testing, let's print it
            println("🔐 OTP for $email: $otpCode")
            _otp.value = otpCode

            _authState.value = AuthState.OtpSent(email)
        }
    }

    /**
     * Verifies the OTP entered by user
     */
    private fun verifyOtp(email: String, enteredOtp: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = otpManager.validateOtp(email, enteredOtp)

            when (result) {
                is ValidationResult.Success -> {
                    analyticsLogger.logOtpValidationSuccess(email)
                    _authState.value = AuthState.Authenticated(
                        email = email,
                        loginTime = System.currentTimeMillis()
                    )
                }

                is ValidationResult.Incorrect -> {
                    val remaining = result.remainingAttempts
                    val message = "Incorrect OTP. $remaining ${if (remaining == 1) "attempt" else "attempts"} remaining"

                    analyticsLogger.logOtpValidationFailure(email, "incorrect_otp")

                    _authState.value = AuthState.OtpError(
                        message = message,
                        email = currentEmail,
                        generatedAt = generatedAt
                    )
                }

                is ValidationResult.MaxAttemptsExceeded -> {
                    analyticsLogger.logOtpValidationFailure(email, "max_attempts")
                    _authState.value = AuthState.OtpError(
                        message = "Maximum attempts exceeded. Please request a new OTP",
                        email = currentEmail,
                        generatedAt = generatedAt
                    )
                }

                is ValidationResult.Expired -> {
                    analyticsLogger.logOtpValidationFailure(email, "expired")
                    _authState.value = AuthState.OtpError("OTP has expired. Please request a new one",email,generatedAt)
                }

                is ValidationResult.NoOtpFound -> {
                    analyticsLogger.logOtpValidationFailure(email, "no_otp_found")
                    _authState.value = AuthState.OtpError("No OTP found. Please request one first",email,generatedAt)
                }
            }
        }
    }

    /**
     * Logs out the user
     */
    private fun logout() {
        viewModelScope.launch {
            val currentState = _authState.value
            if (currentState is AuthState.Authenticated) {
                val sessionDuration = (System.currentTimeMillis() - currentState.loginTime) / 1000
                analyticsLogger.logUserLogout(currentState.email, sessionDuration)
            }

            _authState.value = AuthState.Initial
            currentEmail = ""
        }
    }

    /**
     * Clears error state
     */
    private fun clearError() {
        if (_authState.value is AuthState.OtpError) {
            _authState.value = AuthState.OtpSent(currentEmail)
        }
    }

    /**
     * Gets remaining time for current OTP
     */
    fun getRemainingTime(email: String): Long {
        return otpManager.getRemainingTime(email)
    }

    /**
     * Simple email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                email.contains("@") &&
                email.contains(".")
    }
}