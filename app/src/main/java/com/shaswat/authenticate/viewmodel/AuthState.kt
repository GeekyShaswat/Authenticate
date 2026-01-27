package com.shaswat.authenticate.viewmodel

/**
 * Represents different states of authentication flow
 */
sealed class AuthState {
    object Initial : AuthState()  // Starting state
    object Loading : AuthState()  // When processing
    data class OtpSent(val email: String,val generatedAt: Long = System.currentTimeMillis()) : AuthState()  // OTP sent successfully
    data class OtpError(val message: String, val email: String, val generatedAt: Long) : AuthState()  // Error in OTP flow
    data class Authenticated(
        val email: String,
        val loginTime: Long
    ) : AuthState()  // Successfully logged in
}

/**
 * UI events that screens can trigger
 */
sealed class AuthEvent {
    data class SendOtp(val email: String) : AuthEvent()
    data class VerifyOtp(val email: String, val otp: String) : AuthEvent()
    object Logout : AuthEvent()
    object ClearError : AuthEvent()
}