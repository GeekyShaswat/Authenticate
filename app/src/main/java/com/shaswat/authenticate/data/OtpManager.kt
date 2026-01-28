package com.shaswat.authenticate.data

import kotlin.random.Random

class OtpManager {

    // This Map stores OTP data for each email
    // Key: Email, Value: OtpData
    private val otpStorage = mutableMapOf<String, OtpData>()

    companion object {
        private const val OTP_LENGTH = 6
        private const val OTP_EXPIRY_DURATION = 60_000L // 60 seconds in milliseconds
    }

    /**
     * Generates a new 6-digit OTP for the given email
     * If an OTP already exists for this email, it gets REPLACED
     * Returns the generated OTP code
     */
    fun generateOtp(email: String): String {
        // Generate random 6-digit number
        val otpCode = Random.nextInt(100_000, 999_999).toString()

        // Store it with current timestamp
        val otpData = OtpData(
            code = otpCode,
            generatedAt = System.currentTimeMillis(),
            attempts = 0
        )

        // Store/Replace in map
        otpStorage[email] = otpData

        return otpCode
    }

    /**
     * Validates the OTP entered by user
     * Returns a ValidationResult indicating success or failure reason
     */
    fun validateOtp(email: String, enteredOtp: String): ValidationResult {
        val otpData = otpStorage[email]
            ?: return ValidationResult.NoOtpFound

        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - otpData.generatedAt

        if (timePassed > OTP_EXPIRY_DURATION) {
            return ValidationResult.Expired
        }
        if (otpData.attempts >= otpData.maxAttempts) {
            return ValidationResult.MaxAttemptsExceeded
        }

        return if (otpData.code == enteredOtp) {
            otpStorage.remove(email)
            ValidationResult.Success
        } else {
            // Increment attempts
            val newAttempts = otpData.attempts + 1
            otpStorage[email] = otpData.copy(attempts = newAttempts)

            val remainingAttempts = otpData.maxAttempts - newAttempts

            if (remainingAttempts > 0) {
                ValidationResult.Incorrect(remainingAttempts = remainingAttempts)
            } else {
                // This was the last attempt, now exceeded
                ValidationResult.MaxAttemptsExceeded
            }
        }
    }

    /**
     * Checks how much time is remaining for current OTP
     * Returns remaining seconds, or 0 if expired/not found
     */
    fun getRemainingTime(email: String): Long {
        val otpData = otpStorage[email] ?: return 0L

        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - otpData.generatedAt
        val timeRemaining = OTP_EXPIRY_DURATION - timePassed

        return if (timeRemaining > 0) timeRemaining / 1000 else 0L // Convert to seconds
    }

    /**
     * Get remaining attempts for an email
     */
    fun getRemainingAttempts(email: String): Int {
        val otpData = otpStorage[email] ?: return 3
        return otpData.maxAttempts - otpData.attempts
    }
}

/**
 * Sealed class to represent different validation outcomes
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    object NoOtpFound : ValidationResult()
    object Expired : ValidationResult()
    object MaxAttemptsExceeded : ValidationResult()
    data class Incorrect(val remainingAttempts: Int) : ValidationResult()
}