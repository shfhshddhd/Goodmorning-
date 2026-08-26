package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioStats
import com.example.data.model.GroupVoiceChat
import com.example.data.model.VoiceParticipant
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GiantMicButton
import com.example.ui.components.ManualGainControl
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDarkBackground
import com.example.ui.theme.TgDarkBorder
import com.example.ui.theme.TgDarkCard
import com.example.ui.theme.TgDarkSurface
import com.example.ui.theme.TgDarkSurfaceVariant
import com.example.ui.theme.TgTextMuted
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import com.example.ui.theme.TgVoiceGreen
import com.example.ui.theme.TgVoiceMutedRed
import com.example.viewmodel.MicInteractionType
import java.util.Locale

@Composable
fun VoiceChatRoomScreen(
    chat: GroupVoiceChat,
    participants: List<VoiceParticipant>,
    isMuted: Boolean,
    isPttPressed: Boolean,
    micMode: MicInteractionType,
    manualGain: Float,
    isRawMode: Boolean,
    audioStats: AudioStats,
    waveform: List<Float>,
    onToggleMute: () -> Unit,
    onPttPressChange: (Boolean) -> Unit,
    onMicModeChanged: (MicInteractionType) -> Unit,
    onGainChanged: (Float) -> Unit,
    onRawModeToggled: (Boolean) -> Unit,
    onLeaveRoom: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onOpenDiagnostics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSpeaking = !isMuted && audioStats.currentRms > 0.03f
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TgDarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Room Header & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TgDarkCard)
                .border(1.dp, TgDarkBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TgBlue)
                        .border(1.dp, TgCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.title.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = TgCyan,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = chat.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TgTextPrimary,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TgVoiceGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RAW OPUS 48KHZ • ${audioStats.hardwareLatencyEstimateMs}ms",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TgVoiceGreen
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDiagnostics) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Diagnostics Inspector",
                        tint = TgCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onOpenArchitecture) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Architecture",
                        tint = TgTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Leave Call Button
                Button(
                    onClick = onLeaveRoom,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TgDarkSurfaceVariant,
                        contentColor = TgVoiceMutedRed
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .border(1.dp, TgVoiceMutedRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .testTag("leave_voice_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Leave",
                        tint = TgVoiceMutedRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Leave", color = TgVoiceMutedRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Speakers / Participants Live Carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SPEAKERS IN ROOM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TgTextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "${participants.count { it.isSpeaking }} talking now",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TgVoiceGreen
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(participants, key = { it.id }) { participant ->
                SpeakerAvatarItem(participant = participant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mic Mode Selector (Toggle Mute vs Push-To-Talk)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(TgDarkCard)
                .border(1.dp, TgDarkBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val isToggleSelected = micMode == MicInteractionType.TOGGLE_MUTE
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isToggleSelected) TgCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isToggleSelected) TgCyan else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onMicModeChanged(MicInteractionType.TOGGLE_MUTE) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = if (isToggleSelected) TgCyan else TgTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Toggle Mute",
                        fontSize = 11.sp,
                        fontWeight = if (isToggleSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToggleSelected) TgCyan else TgTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            val isPttSelected = micMode == MicInteractionType.PUSH_TO_TALK
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPttSelected) TgCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isPttSelected) TgCyan else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onMicModeChanged(MicInteractionType.PUSH_TO_TALK) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = if (isPttSelected) TgCyan else TgTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Push-To-Talk (PTT)",
                        fontSize = 11.sp,
                        fontWeight = if (isPttSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isPttSelected) TgCyan else TgTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Giant Mic Button (Center Attraction)
        GiantMicButton(
            isMuted = isMuted,
            isSpeaking = isSpeaking,
            rmsLevel = audioStats.currentRms,
            micMode = micMode,
            onToggleMute = onToggleMute,
            onPttPressChange = onPttPressChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mic Status Guidance Text
        Text(
            text = when {
                micMode == MicInteractionType.PUSH_TO_TALK && isPttPressed -> "TRANSMITTING (PTT Active)"
                micMode == MicInteractionType.PUSH_TO_TALK -> "HOLD MIC TO TRANSMIT"
                !isMuted && isSpeaking -> "LIVE • TRANSMITTING AUDIO"
                !isMuted -> "MIC UNMUTED (Listening)"
                else -> "MIC MUTED • TAP TO TALK"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            color = when {
                !isMuted && isSpeaking -> TgVoiceGreen
                !isMuted -> TgCyan
                else -> TgVoiceMutedRed
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Audio Waveform & RMS Decibel Meter
        AudioWaveformVisualizer(
            waveform = waveform,
            currentDbFs = audioStats.currentDbFs,
            isMuted = isMuted,
            isClipping = audioStats.isClipping
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Mic Gain Control & Raw Mode Toggle
        ManualGainControl(
            gainMultiplier = manualGain,
            isRawMode = isRawMode,
            onGainChanged = onGainChanged,
            onRawModeToggled = onRawModeToggled
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Low-Latency Audio Pipeline Technical Diagnostics Card
        Card(
            colors = CardDefaults.cardColors(containerColor = TgDarkCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TgDarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = TgCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AUDIO ENGINE METRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TgTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "${audioStats.hardwareLatencyEstimateMs} ms Latency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TgVoiceGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Sample Rate", fontSize = 10.sp, color = TgTextMuted)
                        Text("${audioStats.sampleRate} Hz", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TgTextPrimary)
                    }
                    Column {
                        Text("Frame Size", fontSize = 10.sp, color = TgTextMuted)
                        Text("${audioStats.frameDurationMs} ms (960s)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TgTextPrimary)
                    }
                    Column {
                        Text("Buffer Size", fontSize = 10.sp, color = TgTextMuted)
                        Text("${audioStats.minBufferSize} B", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TgTextPrimary)
                    }
                    Column {
                        Text("DSP Bypass", fontSize = 10.sp, color = TgTextMuted)
                        Text(if (isRawMode) "100% RAW" else "HW ACTIVE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = if (isRawMode) TgVoiceGreen else TgCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakerAvatarItem(participant: VoiceParticipant) {
    val ringColor by animateColorAsState(
        targetValue = if (participant.isSpeaking) TgVoiceGreen else if (participant.isMuted) TgVoiceMutedRed else Color.Transparent,
        label = "participant_ring"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(participant.avatarColorHex))
                .border(2.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = participant.name.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
            if (participant.isMuted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(TgDarkBackground)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Muted",
                        tint = TgVoiceMutedRed,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (participant.isSelf) "You" else participant.name.split(" ").first(),
            fontSize = 11.sp,
            color = if (participant.isSpeaking) TgVoiceGreen else TgTextSecondary,
            fontWeight = if (participant.isSpeaking) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
