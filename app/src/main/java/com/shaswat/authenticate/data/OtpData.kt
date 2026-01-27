package com.shaswat.authenticate.data

data class OtpData(
    val code: String,              // The 6-digit OTP
    val generatedAt: Long,         // Timestamp when OTP was created (in milliseconds)
    val attempts: Int = 0,         // How many times user tried to enter OTP
    val maxAttempts: Int = 3       // Maximum allowed attempts
)