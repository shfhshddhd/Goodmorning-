package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.AudioStats
import com.example.data.model.AuthState
import com.example.data.model.GroupVoiceChat
import com.example.data.model.VoiceParticipant
import com.example.data.telegram.TelegramClientBridge
import com.example.service.VoiceChatForegroundService
import com.example.telegram.tgcalls.TgCallsStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MicInteractionType {
    TOGGLE_MUTE,
    PUSH_TO_TALK
}

class MainViewModel(private val application: Application) : AndroidViewModel(application) {

    val telegramBridge = TelegramClientBridge(application, viewModelScope)
    val audioEngine = AudioEngine(application)

    val authState: StateFlow<AuthState> = telegramBridge.authState
    val activeVoiceChats: StateFlow<List<GroupVoiceChat>> = telegramBridge.activeVoiceChats
    val currentJoinedCall: StateFlow<GroupVoiceChat?> = telegramBridge.currentJoinedCall
    val participants: StateFlow<List<VoiceParticipant>> = telegramBridge.participants
    val protocolLogs: StateFlow<List<String>> = telegramBridge.logsFlow
    val tgCallsStats: StateFlow<TgCallsStats> = telegramBridge.groupCallEngine.stats

    val audioStats: StateFlow<AudioStats> = audioEngine.audioStats
    val waveformFlow: StateFlow<List<Float>> = audioEngine.waveformFlow

    // Mic interaction mode
    private val _micInteractionType = MutableStateFlow(MicInteractionType.TOGGLE_MUTE)
    val micInteractionType: StateFlow<MicInteractionType> = _micInteractionType.asStateFlow()

    // Push to talk active state
    private val _isPttPressed = MutableStateFlow(false)
    val isPttPressed: StateFlow<Boolean> = _isPttPressed.asStateFlow()

    // Mic Muted state (Default is muted upon joining)
    private val _isMuted = MutableStateFlow(true)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Manual Mic Gain (0.1x to 10.0x) - default 1.0x
    private val _manualGain = MutableStateFlow(1.0f)
    val manualGain: StateFlow<Float> = _manualGain.asStateFlow()

    // Raw mode (Bypasses all Android AGC/NS/AEC) - default TRUE as requested
    private val _isRawMode = MutableStateFlow(true)
    val isRawMode: StateFlow<Boolean> = _isRawMode.asStateFlow()

    // Search query in active chats
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Architecture info sheet open state
    private val _showArchitectureDialog = MutableStateFlow(false)
    val showArchitectureDialog: StateFlow<Boolean> = _showArchitectureDialog.asStateFlow()

    // Real Audio Diagnostics screen visibility state
    private val _showDiagnosticScreen = MutableStateFlow(false)
    val showDiagnosticScreen: StateFlow<Boolean> = _showDiagnosticScreen.asStateFlow()

    init {
        audioEngine.setMuted(true)
        audioEngine.setRawMode(true)
        audioEngine.setMicGain(1.0f)
        telegramBridge.groupCallEngine.setMuted(true)
        telegramBridge.groupCallEngine.setManualGain(1.0f)

        // Mandatory Link: Feed low-latency raw 48 kHz PCM frames directly into tgcalls group call media pipeline
        audioEngine.onAudioFrameCaptured = { pcmData, rms, isSpeaking ->
            val muted = audioEngine.isCurrentlyMuted()
            telegramBridge.setSelfSpeaking(isSpeaking = isSpeaking && !muted, isMuted = muted)
            telegramBridge.groupCallEngine.pushAudioFrame(pcmData, rms, isSpeaking)
        }
    }

    fun onPhoneNumberSubmitted(phone: String) {
        telegramBridge.sendPhoneNumber(phone)
    }

    fun onAuthCodeSubmitted(code: String) {
        telegramBridge.checkAuthCode(code)
    }

    fun onCloudPasswordSubmitted(password: String) {
        telegramBridge.checkCloudPassword(password)
    }

    fun onQuickDemoLogin() {
        telegramBridge.loginWithDemoProfile()
    }

    fun onLogout() {
        audioEngine.stopCapture()
        telegramBridge.logout()
        VoiceChatForegroundService.stopService(application)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun joinVoiceChat(chat: GroupVoiceChat) {
        telegramBridge.joinVoiceChat(chat)
        _isMuted.value = true
        audioEngine.setMuted(true)
        telegramBridge.groupCallEngine.setMuted(true)
        audioEngine.startCapture()
        VoiceChatForegroundService.startService(application, chat.title)
    }

    fun leaveVoiceChat() {
        audioEngine.stopCapture()
        telegramBridge.leaveVoiceChat()
        _isMuted.value = true
        VoiceChatForegroundService.stopService(application)
    }

    fun toggleMute() {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        audioEngine.setMuted(newMuted)
        telegramBridge.groupCallEngine.setMuted(newMuted)
        telegramBridge.setSelfSpeaking(isSpeaking = false, isMuted = newMuted)
    }

    fun setPttPressed(pressed: Boolean) {
        _isPttPressed.value = pressed
        val unmuted = pressed
        _isMuted.value = !unmuted
        audioEngine.setMuted(!unmuted)
        telegramBridge.groupCallEngine.setMuted(!unmuted)
        telegramBridge.setSelfSpeaking(isSpeaking = unmuted, isMuted = !unmuted)
    }

    fun setMicInteractionType(type: MicInteractionType) {
        _micInteractionType.value = type
        if (type == MicInteractionType.PUSH_TO_TALK) {
            _isMuted.value = true
            audioEngine.setMuted(true)
            telegramBridge.groupCallEngine.setMuted(true)
        }
    }

    fun setManualGain(gain: Float) {
        _manualGain.value = gain
        audioEngine.setMicGain(gain)
        telegramBridge.groupCallEngine.setManualGain(gain)
    }

    fun setRawMode(enabled: Boolean) {
        _isRawMode.value = enabled
        audioEngine.setRawMode(enabled)
    }

    fun setHardwareDsp(agc: Boolean, ns: Boolean, aec: Boolean) {
        audioEngine.setHardwareDsp(agc = agc, ns = ns, aec = aec)
        _isRawMode.value = audioEngine.audioStats.value.rawModeEnabled
    }

    fun setShowArchitectureDialog(show: Boolean) {
        _showArchitectureDialog.value = show
    }

    fun setShowDiagnosticScreen(show: Boolean) {
        _showDiagnosticScreen.value = show
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopCapture()
        VoiceChatForegroundService.stopService(application)
    }
}
