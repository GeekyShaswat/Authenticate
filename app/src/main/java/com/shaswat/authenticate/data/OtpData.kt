package com.shaswat.authenticate.data

data class OtpData(
    val code: String,
    val generatedAt: Long,
    val attempts: Int = 0,
    val maxAttempts: Int = 3
)