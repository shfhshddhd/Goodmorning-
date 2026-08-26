package com.example.data.model

data class VoiceParticipant(
    val id: Long,
    val name: String,
    val username: String? = null,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val isSelf: Boolean = false,
    val volumeLevel: Float = 0.8f,
    val isHandRaised: Boolean = false,
    val pingMs: Int = 24,
    val avatarColorHex: Long = 0xFF2AABEE
)
