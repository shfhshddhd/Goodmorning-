package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AuthState
import com.example.ui.components.TechnicalArchitectureDialog
import com.example.ui.screens.ActiveChatsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.VoiceChatRoomScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TgDarkBackground
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasRecordAudioPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasRecordAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val activeChats by viewModel.activeVoiceChats.collectAsStateWithLifecycle()
    val currentJoinedCall by viewModel.currentJoinedCall.collectAsStateWithLifecycle()
    val participants by viewModel.participants.collectAsStateWithLifecycle()
    val protocolLogs by viewModel.protocolLogs.collectAsStateWithLifecycle()
    val audioStats by viewModel.audioStats.collectAsStateWithLifecycle()
    val waveform by viewModel.waveformFlow.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isPttPressed by viewModel.isPttPressed.collectAsStateWithLifecycle()
    val micMode by viewModel.micInteractionType.collectAsStateWithLifecycle()
    val manualGain by viewModel.manualGain.collectAsStateWithLifecycle()
    val isRawMode by viewModel.isRawMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showArchitectureDialog by viewModel.showArchitectureDialog.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(TgDarkBackground)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TgDarkBackground)
        ) {
            when {
                // Screen 3: Joined in a Group Voice Chat Room
                currentJoinedCall != null -> {
                    VoiceChatRoomScreen(
                        chat = currentJoinedCall!!,
                        participants = participants,
                        isMuted = isMuted,
                        isPttPressed = isPttPressed,
                        micMode = micMode,
                        manualGain = manualGain,
                        isRawMode = isRawMode,
                        audioStats = audioStats,
                        waveform = waveform,
                        onToggleMute = {
                            if (!hasRecordAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.toggleMute()
                            }
                        },
                        onPttPressChange = { pressed ->
                            if (!hasRecordAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.setPttPressed(pressed)
                            }
                        },
                        onMicModeChanged = { viewModel.setMicInteractionType(it) },
                        onGainChanged = { viewModel.setManualGain(it) },
                        onRawModeToggled = { viewModel.setRawMode(it) },
                        onLeaveRoom = { viewModel.leaveVoiceChat() },
                        onOpenArchitecture = { viewModel.setShowArchitectureDialog(true) }
                    )
                }

                // Screen 2: Authenticated - Show only Active Voice Chats
                authState is AuthState.Ready -> {
                    ActiveChatsScreen(
                        userAuthState = authState as AuthState.Ready,
                        activeChats = activeChats,
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onJoinChat = { chat ->
                            if (!hasRecordAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                            viewModel.joinVoiceChat(chat)
                        },
                        onRefresh = { viewModel.telegramBridge.fetchActiveVoiceChats() },
                        onOpenArchitecture = { viewModel.setShowArchitectureDialog(true) },
                        onLogout = { viewModel.onLogout() }
                    )
                }

                // Screen 1: Telegram Phone / OTP / 2FA Login
                else -> {
                    AuthScreen(
                        authState = authState,
                        onSendPhoneNumber = { viewModel.onPhoneNumberSubmitted(it) },
                        onSendCode = { viewModel.onAuthCodeSubmitted(it) },
                        onSendPassword = { viewModel.onCloudPasswordSubmitted(it) },
                        onQuickDemoLogin = { viewModel.onQuickDemoLogin() },
                        onOpenArchitecture = { viewModel.setShowArchitectureDialog(true) },
                        onResetToPhone = { viewModel.telegramBridge.resetToPhoneNumberInput() },
                        apiId = viewModel.telegramBridge.getApiId(),
                        apiHash = viewModel.telegramBridge.getApiHash(),
                        onUpdateApiCredentials = { id, hash -> viewModel.telegramBridge.setApiCredentials(id, hash) }
                    )
                }
            }

            // In-app Telegram Architecture & Protocol Limits Dialog
            if (showArchitectureDialog) {
                TechnicalArchitectureDialog(
                    protocolLogs = protocolLogs,
                    onDismiss = { viewModel.setShowArchitectureDialog(false) }
                )
            }
        }
    }
}
