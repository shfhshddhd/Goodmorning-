package com.example.data.model

sealed interface AuthState {
    data object LoggedOut : AuthState
    data object WaitPhoneNumber : AuthState
    data class WaitCode(
        val phoneNumber: String,
        val phoneCodeHash: String,
        val timeoutSeconds: Int = 60,
        val isTestMode: Boolean = false
    ) : AuthState
    data class WaitPassword(
        val hint: String = "Telegram 2FA Cloud Password"
    ) : AuthState
    data class Ready(
        val userId: Long,
        val firstName: String,
        val lastName: String = "",
        val username: String? = null,
        val phoneNumber: String = "",
        val isPremium: Boolean = false
    ) : AuthState
    data class Error(val message: String) : AuthState
}
