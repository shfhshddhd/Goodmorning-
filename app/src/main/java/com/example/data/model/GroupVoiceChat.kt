package com.example.data.model

data class GroupVoiceChat(
    val id: Long,
    val chatId: Long,
    val title: String,
    val username: String? = null,
    val activeParticipantsCount: Int,
    val speakersCount: Int,
    val isLive: Boolean = true,
    val pinnedTopic: String? = null,
    val accessHash: Long = 0L,
    val streamDcId: Int = 2,
    val ssrc: Int = 1001,
    val isJoined: Boolean = false,
    val currentPingMs: Int = 22,
    val avatarColorHex: Long = 0xFF2AABEE,
    val activeSpeakers: List<VoiceParticipant> = emptyList()
)
