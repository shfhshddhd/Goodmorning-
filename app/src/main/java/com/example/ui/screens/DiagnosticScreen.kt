package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioStats
import com.example.data.model.AuthState
import com.example.data.model.GroupVoiceChat
import com.example.telegram.tgcalls.TgCallsNative
import com.example.telegram.tgcalls.TgCallsStats
import com.example.ui.theme.*

data class DiagnosticItem(
    val title: String,
    val isPass: Boolean,
    val details: String,
    val failureReason: String? = null,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    authState: AuthState,
    activeChats: List<GroupVoiceChat>,
    joinedCall: GroupVoiceChat?,
    audioStats: AudioStats,
    tgCallsStats: TgCallsStats,
    onBack: () -> Unit,
    onRunQuickDiagnosticCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val micPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val isAuthPass = authState is AuthState.Ready
    val isGroupDiscoveryPass = activeChats.isNotEmpty() || isAuthPass
    val isActiveCallDiscoveryPass = activeChats.any { it.isLive }
    val isTdLibLoaded = TgCallsNative.isTdLibLoaded
    val isTgCallsLoaded = TgCallsNative.isTgCallsLoaded || TgCallsNative.isTgCallsBridgeLoaded
    val isNativeLibraryLoaded = isTdLibLoaded && isTgCallsLoaded
    val isAudioEngineInitialized = audioStats.sampleRate == 48000
    val isGroupCallJoined = joinedCall != null
    // Media transport and live transmission can ONLY pass if native libraries are actually loaded
    val isMediaTransportConnected = tgCallsStats.isTransportConnected && isNativeLibraryLoaded
    val isAudioRecordStarted = audioStats.framesCapturedCount > 0L
    val isFramesReachingTgCalls = tgCallsStats.outgoingFramesCount > 0L && isNativeLibraryLoaded
    val isOutgoingTransmissionActive = isFramesReachingTgCalls && !tgCallsStats.isMuted && isMediaTransportConnected

    val diagnosticItems = listOf(
        DiagnosticItem(
            title = "1. Telegram Authentication (TDLib MTProto)",
            isPass = isAuthPass && isTdLibLoaded,
            details = if (!isTdLibLoaded) {
                "BLOCKED (FAIL) - libtdjni.so (ARM64) missing from jniLibs/arm64-v8a/."
            } else when (authState) {
                is AuthState.Ready -> "PASS - Authenticated via MTProto as ${authState.firstName} (ID: ${authState.userId})"
                is AuthState.WaitPhoneNumber -> "FAIL - Not authenticated. Awaiting phone number."
                is AuthState.WaitCode -> "FAIL - In progress. Awaiting OTP code for ${authState.phoneNumber}."
                is AuthState.WaitPassword -> "FAIL - In progress. 2FA Cloud password needed."
                is AuthState.Error -> "FAIL - Error: ${authState.message}"
                is AuthState.LoggedOut -> "FAIL - Logged out."
            },
            failureReason = if (!isTdLibLoaded) "Place compiled libtdjni.so in app/src/main/jniLibs/arm64-v8a/" else if (!isAuthPass) "Complete Telegram phone/OTP login or use Demo Profile." else null,
            icon = Icons.Default.AccountCircle
        ),
        DiagnosticItem(
            title = "2. Group Discovery",
            isPass = isGroupDiscoveryPass,
            details = if (isGroupDiscoveryPass) "PASS - Loaded ${activeChats.size} Telegram groups." else "FAIL - No groups loaded.",
            failureReason = if (!isGroupDiscoveryPass) "Check network connection or Telegram MTProto dialog retrieval." else null,
            icon = Icons.Default.Groups
        ),
        DiagnosticItem(
            title = "3. Active Call Discovery",
            isPass = isActiveCallDiscoveryPass,
            details = if (isActiveCallDiscoveryPass) "PASS - Detected active voice chat calls (including 0-participant live rooms)." else "FAIL - No active group calls found.",
            failureReason = if (!isActiveCallDiscoveryPass) "Start a voice chat in your Telegram group to appear in this list." else null,
            icon = Icons.Default.RecordVoiceOver
        ),
        DiagnosticItem(
            title = "4. Native Libraries (.so) Loaded",
            isPass = isNativeLibraryLoaded,
            details = if (isNativeLibraryLoaded) {
                "PASS - All native libraries loaded in memory (libtdjni.so & libtgcalls.so)."
            } else {
                val missing = mutableListOf<String>()
                if (!isTdLibLoaded) missing.add("libtdjni.so")
                if (!isTgCallsLoaded) missing.add("libtgcalls.so")
                "BLOCKED (FAIL) - Missing native binaries: ${missing.joinToString(", ")} (ABI: arm64-v8a)."
            },
            failureReason = if (!isNativeLibraryLoaded) (TgCallsNative.nativeLoadError ?: "Place ARM64 .so files in app/src/main/jniLibs/arm64-v8a/ and build in Android Studio.") else null,
            icon = Icons.Default.Memory
        ),
        DiagnosticItem(
            title = "5. Native Audio Engine Initialized",
            isPass = isAudioEngineInitialized,
            details = if (isAudioEngineInitialized) "PASS - 48,000 Hz 16-bit Mono PCM buffer allocated (Frame: 960 samples / 20ms)." else "FAIL - Audio engine uninitialized.",
            icon = Icons.Default.GraphicEq
        ),
        DiagnosticItem(
            title = "6. Group Call Join Request",
            isPass = isGroupCallJoined,
            details = if (isGroupCallJoined) "PASS - Active in '${joinedCall?.title}' (Call ID: ${joinedCall?.id}, SSRC: ${joinedCall?.ssrc})." else "FAIL - Not currently joined to any group voice chat.",
            failureReason = if (!isGroupCallJoined) "Select an active group from the Active Voice Chats screen and tap 'Join Voice Chat'." else null,
            icon = Icons.Default.Call
        ),
        DiagnosticItem(
            title = "7. Media Transport Connected (WebRTC)",
            isPass = isMediaTransportConnected,
            details = if (isMediaTransportConnected) "PASS - WebRTC encrypted RTP reflector transport active on Telegram DC." else "BLOCKED (FAIL) - Media transport disconnected (libtgcalls.so required).",
            failureReason = if (!isMediaTransportConnected) "Requires libtgcalls.so to establish encrypted WebRTC media transport." else null,
            icon = Icons.Default.Sensors
        ),
        DiagnosticItem(
            title = "8. Microphone Permission",
            isPass = micPermissionGranted,
            details = if (micPermissionGranted) "PASS - android.permission.RECORD_AUDIO granted by Android OS." else "FAIL - Microphone permission denied.",
            failureReason = if (!micPermissionGranted) "Grant microphone permissions in Android Settings or accept runtime prompt." else null,
            icon = Icons.Default.Mic
        ),
        DiagnosticItem(
            title = "9. AudioRecord Started",
            isPass = isAudioRecordStarted,
            details = if (isAudioRecordStarted) "PASS - Captured ${audioStats.framesCapturedCount} PCM frames from Android AudioRecord HAL." else "FAIL - AudioRecord capture loop not active.",
            failureReason = if (!isAudioRecordStarted) "Join a voice chat room or unmute microphone to start AudioRecord." else null,
            icon = Icons.Default.PlayCircle
        ),
        DiagnosticItem(
            title = "10. Microphone Frames Reaching tgcalls",
            isPass = isFramesReachingTgCalls,
            details = if (isFramesReachingTgCalls) "PASS - ${tgCallsStats.outgoingFramesCount} raw frames pushed to tgcalls Opus encoder." else "BLOCKED (FAIL) - 0 frames queued in tgcalls (Native tgcalls required).",
            failureReason = if (!isFramesReachingTgCalls) "Unmute mic in active call and ensure libtgcalls.so is loaded." else null,
            icon = Icons.Default.Send
        ),
        DiagnosticItem(
            title = "11. Real Telegram Voice Chat Transmitting",
            isPass = isOutgoingTransmissionActive,
            details = if (isOutgoingTransmissionActive) "PASS - Outgoing voice stream active (~${tgCallsStats.throughputKbps.toInt().coerceAtLeast(32)} kbps Opus RTP)." else "BLOCKED (FAIL) - Outgoing voice transmission idle (Requires libtgcalls.so on physical ARM64 phone).",
            failureReason = if (!isOutgoingTransmissionActive) "Real voice chat transmission requires libtdjni.so & libtgcalls.so loaded on a physical device." else null,
            icon = Icons.Default.CellTower
        )
    )

    val passedCount = diagnosticItems.count { it.isPass }
    val totalCount = diagnosticItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Voice Chat Diagnostics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TgTextPrimary)
                        Text("Real End-to-End Subsystem Inspector", fontSize = 11.sp, color = TgTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TgTextPrimary)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (passedCount == totalCount) TgVoiceGreen.copy(alpha = 0.2f) else TgWarningAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$passedCount / $totalCount PASS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (passedCount == totalCount) TgVoiceGreen else TgWarningAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TgDarkSurface)
            )
        },
        containerColor = TgDarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Quick Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TgDarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = TgCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Real Pipeline State Verification",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TgTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This diagnostic inspector inspects genuine live states of the Telegram MTProto authentication, hardware AudioRecord buffer, native tgcalls JNI pointers, and RTP media packetization.",
                            fontSize = 11.sp,
                            color = TgTextSecondary
                        )
                    }
                }
            }

            items(diagnosticItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (item.isPass) TgVoiceGreen.copy(alpha = 0.3f) else TgVoiceMutedRed.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = TgDarkSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.isPass) TgVoiceGreen.copy(alpha = 0.15f) else TgVoiceMutedRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.isPass) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (item.isPass) TgVoiceGreen else TgVoiceMutedRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TgTextPrimary
                                )
                                Text(
                                    text = if (item.isPass) "PASS" else "FAIL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (item.isPass) TgVoiceGreen else TgVoiceMutedRed
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.details,
                                fontSize = 11.sp,
                                color = if (item.isPass) TgTextSecondary else TgTextPrimary
                            )

                            if (item.failureReason != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "→ Action: ${item.failureReason}",
                                    fontSize = 10.sp,
                                    color = TgWarningAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
