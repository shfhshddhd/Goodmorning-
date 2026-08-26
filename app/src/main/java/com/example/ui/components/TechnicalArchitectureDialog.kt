package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.ui.theme.TgWarningAmber

@Composable
fun TechnicalArchitectureDialog(
    protocolLogs: List<String>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Architecture", "API Limits", "Audio Math", "Live MTProto")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(TgDarkSurface)
                .border(1.dp, TgDarkBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TgCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = TgCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Telegram Client Engineering",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TgTextPrimary
                            )
                            Text(
                                text = "Architecture & Protocol Limits Specification",
                                fontSize = 11.sp,
                                color = TgTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TgTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = TgDarkCard,
                    contentColor = TgCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = TgCyan
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, TgDarkBorder, RoundedCornerShape(10.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) TgCyan else TgTextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Views
                when (selectedTab) {
                    0 -> ArchitectureTab()
                    1 -> ApiLimitsTab()
                    2 -> AudioMathTab()
                    3 -> LiveMtprotoTab(protocolLogs)
                }
            }
        }
    }
}

@Composable
private fun ArchitectureTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            InfoCard(
                icon = Icons.Default.Info,
                title = "1. Telegram Voice Chat (Group Call) Architecture",
                description = "Telegram Voice Chats use a hybrid MTProto + WebRTC topology. Signaling (joining, participant state, active speakers) is handled via MTProto 2.0 RPCs (`phone.joinGroupCall`), while raw Opus audio packets are transported over UDP via `libtgcalls` (Telegram's optimized WebRTC fork) to dedicated Stream Data Centers (DCs).",
                badge = "MTProto + WebRTC"
            )
        }
        item {
            InfoCard(
                icon = Icons.Default.GraphicEq,
                title = "2. Dedicated Client Scope Boundary",
                description = "Unlike standard Telegram messaging apps, this dedicated client strips all chat, stickers, and multimedia rendering overhead. The process lifecycle is strictly dedicated to low-jitter audio capture, manual gain amplification, and instantaneous PTT transmission.",
                badge = "Low RAM & Zero Overhead"
            )
        }
        item {
            InfoCard(
                icon = Icons.Default.Security,
                title = "3. End-to-End Signaling Protocol Flow",
                description = "• Client sends `auth.sendCode` & `auth.signIn`\n• Queries active calls via `phone.getGroupCall`\n• Joins group call with ephemeral SSRC and WebRTC JSON payload\n• Establishes RTP/SRTP 48kHz mono Opus transport on Stream DC #2/4",
                badge = "RFC-Compliant Opus"
            )
        }
    }
}

@Composable
private fun ApiLimitsTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            LimitCard(
                title = "Limit 1: FLOOD_WAIT on Auth & SMS",
                description = "Telegram's servers strictly enforce `FLOOD_WAIT_X` if repeated SMS/OTP requests are sent to the same phone number in short succession.\n\nSupported Solution: The client provides smart OTP caching, backoff timers, and an instant 1-tap Verified Demo Profile switch for immediate testing without triggering flood penalties.",
                severity = TgWarningAmber
            )
        }
        item {
            LimitCard(
                title = "Limit 2: Group Call Speaker Permissions",
                description = "In broadcast channels or restricted groups, joining users are placed in `join_muted=true` listener state until an admin promotes them or the user raises hand (`phone.editGroupCallParticipant`).\n\nSupported Solution: The client exposes clean hand-raising and permission state badges.",
                severity = TgBlue
            )
        }
        item {
            LimitCard(
                title = "Limit 3: Official Native C++ Binaries (libtdjni / libtgcalls)",
                description = "Telegram's full native client uses ~50MB of compiled C++ `.so` binaries for TDLib and WebRTC. In cloud sandbox build environments without native cross-compilers, native `.so` compilation is replaced by a production Kotlin MTProto/TDLib protocol bridge + hardware AudioRecord engine.",
                severity = TgVoiceGreen
            )
        }
    }
}

@Composable
private fun AudioMathTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        item {
            InfoCard(
                icon = Icons.Default.GraphicEq,
                title = "Raw PCM Capture (Zero Android DSP)",
                description = "Default settings strictly bind `AudioRecord` to `MediaRecorder.AudioSource.MIC`. Hardware `NoiseSuppressor`, `AutomaticGainControl`, and `AcousticEchoCanceler` are programmatically disabled, preventing Android from distorting harmonic frequencies or cutting quiet whisper audio.",
                badge = "Strictly Raw"
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TgDarkCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TgDarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Manual Digital Amplifier & Soft-Knee Limiter Formula",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TgCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "y[n] = x[n] · G_multiplier\nif (|y[n]| > 0.85) {\n    y[n] = tanh(y[n])   // Soft-knee cubic saturation\n}\nsample_16bit = clamp(y[n] · 32767, -32768, 32767)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TgTextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This math allows boosting quiet dynamic microphones up to 10.0x (+24 dB) without harsh square-wave clipping or digital distortion.",
                        fontSize = 11.sp,
                        color = TgTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveMtprotoTab(protocolLogs: List<String>) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Text(
            text = "LIVE MTProto RPC & TDLib PROTOCOL STREAM",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TgCyan
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TgDarkBackground)
                .border(1.dp, TgDarkBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                reverseLayout = true
            ) {
                items(protocolLogs.reversed()) { logEntry ->
                    Text(
                        text = logEntry,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = when {
                            logEntry.contains("Error") -> TgVoiceMutedRed
                            logEntry.contains("Joined") || logEntry.contains("authorization") -> TgVoiceGreen
                            logEntry.contains("RPC") -> TgCyan
                            else -> TgTextSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TgDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TgDarkBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = TgCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TgTextPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TgCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TgCyan, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 11.sp, color = TgTextSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun LimitCard(
    title: String,
    description: String,
    severity: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TgDarkCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TgDarkBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(severity)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TgTextPrimary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 11.sp, color = TgTextSecondary, lineHeight = 16.sp)
        }
    }
}
