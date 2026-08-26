package com.example

import com.example.audio.AudioStats
import com.example.data.model.AuthState
import com.example.data.model.GroupVoiceChat
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.log10

class AudioAndTelegramLogicTest {

    @Test
    fun testActiveVoiceChatFilter_includesZeroParticipants() {
        val groups = listOf(
            GroupVoiceChat(
                id = 1L,
                chatId = 101L,
                title = "Live Room Active",
                activeParticipantsCount = 12,
                speakersCount = 2,
                isLive = true
            ),
            GroupVoiceChat(
                id = 2L,
                chatId = 102L,
                title = "Empty Voice Chat Room",
                activeParticipantsCount = 0, // 0 participants but voice chat is active
                speakersCount = 0,
                isLive = true
            )
        )

        // Filter criteria: isLive must remain visible even with 0 participants
        val filtered = groups.filter { it.isLive }
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.activeParticipantsCount == 0 })
    }

    @Test
    fun testManualGainDbCalculation() {
        val gain1x = 1.0f
        val db1x = 20.0f * log10(gain1x)
        assertEquals(0.0f, db1x, 0.001f)

        val gain2x = 2.0f
        val db2x = 20.0f * log10(gain2x)
        assertEquals(6.020f, db2x, 0.01f)

        val gain0_5x = 0.5f
        val db0_5x = 20.0f * log10(gain0_5x)
        assertEquals(-6.020f, db0_5x, 0.01f)
    }

    @Test
    fun testAuthStateTransitions() {
        var state: AuthState = AuthState.WaitPhoneNumber
        assertTrue(state is AuthState.WaitPhoneNumber)

        state = AuthState.WaitCode(
            phoneNumber = "+919876543210",
            phoneCodeHash = "abcd1234",
            timeoutSeconds = 60
        )
        assertTrue(state is AuthState.WaitCode)
        assertEquals("+919876543210", (state as AuthState.WaitCode).phoneNumber)

        state = AuthState.Ready(
            userId = 123456L,
            firstName = "TestUser",
            lastName = "",
            username = "test_user",
            phoneNumber = "+919876543210"
        )
        assertTrue(state is AuthState.Ready)
        assertEquals("TestUser", (state as AuthState.Ready).firstName)
    }
}
