package com.shaswat.authenticate.analytics

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent


class AnalyticsLogger(context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    companion object {
        private const val EVENT_OTP_GENERATED = "otp_generated"
        private const val EVENT_OTP_VALIDATION_SUCCESS = "otp_validation_success"
        private const val EVENT_OTP_VALIDATION_FAILURE = "otp_validation_failure"
        private const val EVENT_USER_LOGOUT = "user_logout"

        private const val PARAM_EMAIL = "email"
        private const val PARAM_FAILURE_REASON = "failure_reason"
    }

    /**
     * Log when OTP is generated for an email
     */
    fun logOtpGenerated(email: String) {
        firebaseAnalytics.logEvent(EVENT_OTP_GENERATED) {
            param(PARAM_EMAIL, maskEmail(email))
        }
    }

    /**
     * Log when OTP validation succeeds
     */
    fun logOtpValidationSuccess(email: String) {
        firebaseAnalytics.logEvent(EVENT_OTP_VALIDATION_SUCCESS) {
            param(PARAM_EMAIL, maskEmail(email))
        }
    }

    /**
     * Log when OTP validation fails
     */
    fun logOtpValidationFailure(email: String, reason: String) {
        firebaseAnalytics.logEvent(EVENT_OTP_VALIDATION_FAILURE) {
            param(PARAM_EMAIL, maskEmail(email))
            param(PARAM_FAILURE_REASON, reason)
        }
    }

    /**
     * Log when user logs out
     */
    fun logUserLogout(email: String, sessionDuration: Long) {
        firebaseAnalytics.logEvent(EVENT_USER_LOGOUT) {
            param(PARAM_EMAIL, maskEmail(email))
            param("session_duration_seconds", sessionDuration)
        }
    }

    /**
     * Masks email for privacy
     */
    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return "invalid_email"

        val username = parts[0]
        val domain = parts[1]

        val maskedUsername = if (username.length > 1) {
            username[0] + "***"
        } else {
            "***"
        }

        return "$maskedUsername@$domain"
    }
}