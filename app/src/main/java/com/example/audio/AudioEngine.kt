package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Ultra-low-latency raw PCM audio capture engine designed specifically for Telegram Voice Chat.
 *
 * Key engineering properties:
 * 1. Default strictly RAW PCM capture — bypasses automatic noise suppression (NS),
 *    auto gain control (AGC), and acoustic echo cancellation (AEC).
 * 2. Manual gain multiplier (0.1x to 10.0x / -20dB to +24dB) with smooth soft-knee tanh limiter.
 * 3. 48,000 Hz, 16-bit Mono (Opus frame matching Telegram WebRTC standard 20ms frames = 960 samples).
 * 4. High-priority audio thread to achieve minimal hardware latency (< 20ms).
 */
class AudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "AudioEngine"
        const val SAMPLE_RATE = 48000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val FRAME_SIZE_SAMPLES = 960 // 20ms at 48kHz
        const val BYTES_PER_SAMPLE = 2
        const val FRAME_SIZE_BYTES = FRAME_SIZE_SAMPLES * BYTES_PER_SAMPLE // 1920 bytes
    }

    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var acousticEchoCanceler: AcousticEchoCanceler? = null
    private var automaticGainControl: AutomaticGainControl? = null

    private val isRecording = AtomicBoolean(false)
    private val isMuted = AtomicBoolean(true)
    private var captureThread: Thread? = null

    // Audio stats & live waveform flow
    private val _audioStats = MutableStateFlow(AudioStats())
    val audioStats: StateFlow<AudioStats> = _audioStats.asStateFlow()

    // Real-time audio waveform amplitudes (32 frequency/amplitude bins for 60fps UI visualizer)
    private val _waveformFlow = MutableStateFlow(List(32) { 0.05f })
    val waveformFlow: StateFlow<List<Float>> = _waveformFlow.asStateFlow()

    // Configurable parameters
    @Volatile private var gainMultiplier: Float = 1.0f
    @Volatile private var isRawMode: Boolean = true
    @Volatile private var enableHardwareAgc: Boolean = false
    @Volatile private var enableHardwareNs: Boolean = false
    @Volatile private var enableHardwareAec: Boolean = false

    // Audio callback for feeding encoded/raw packets into Telegram connection bridge
    var onAudioFrameCaptured: ((pcmData: ByteArray, rms: Float, isSpeaking: Boolean) -> Unit)? = null

    /**
     * Updates manual digital gain multiplier (0.1x to 10.0x)
     */
    fun setMicGain(gain: Float) {
        val clampedGain = gain.coerceIn(0.1f, 10.0f)
        gainMultiplier = clampedGain
        _audioStats.value = _audioStats.value.copy(gainMultiplier = clampedGain)
    }

    /**
     * Toggles Raw Mode (Strictly disable all hardware DSP by default)
     */
    fun setRawMode(enabled: Boolean) {
        isRawMode = enabled
        if (enabled) {
            enableHardwareAgc = false
            enableHardwareNs = false
            enableHardwareAec = false
            applyHardwareEffects()
        }
        _audioStats.value = _audioStats.value.copy(
            rawModeEnabled = enabled,
            hardwareAgcActive = enableHardwareAgc,
            hardwareNsActive = enableHardwareNs,
            hardwareAecActive = enableHardwareAec
        )
    }

    /**
     * Explicit hardware DSP toggles (only activated if user turns off Raw Mode and turns these on)
     */
    fun setHardwareDsp(agc: Boolean, ns: Boolean, aec: Boolean) {
        if (isRawMode && (agc || ns || aec)) {
            isRawMode = false
        }
        enableHardwareAgc = agc
        enableHardwareNs = ns
        enableHardwareAec = aec
        applyHardwareEffects()
        _audioStats.value = _audioStats.value.copy(
            rawModeEnabled = isRawMode,
            hardwareAgcActive = enableHardwareAgc,
            hardwareNsActive = enableHardwareNs,
            hardwareAecActive = enableHardwareAec
        )
    }

    fun setMuted(muted: Boolean) {
        isMuted.set(muted)
    }

    fun isCurrentlyMuted(): Boolean = isMuted.get()

    @SuppressLint("MissingPermission")
    fun startCapture() {
        if (isRecording.get()) return

        try {
            val minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            val bufferSize = max(minBufSize, FRAME_SIZE_BYTES * 2)

            // AudioSource.MIC gives pure unprocessed capture on standard Android HAL
            val audioSource = if (isRawMode) MediaRecorder.AudioSource.MIC else MediaRecorder.AudioSource.VOICE_COMMUNICATION

            audioRecord = AudioRecord(
                audioSource,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed!")
                return
            }

            val audioSessionId = audioRecord?.audioSessionId ?: 0
            initHardwareEffects(audioSessionId)

            audioRecord?.startRecording()
            isRecording.set(true)

            _audioStats.value = _audioStats.value.copy(
                sampleRate = SAMPLE_RATE,
                minBufferSize = bufferSize,
                gainMultiplier = gainMultiplier,
                rawModeEnabled = isRawMode
            )

            captureThread = Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                val pcmShortBuffer = ShortArray(FRAME_SIZE_SAMPLES)
                val pcmByteBuffer = ByteArray(FRAME_SIZE_BYTES)
                var frameCount = 0L
                var underruns = 0

                while (isRecording.get()) {
                    val record = audioRecord ?: break
                    val readCount = record.read(pcmShortBuffer, 0, FRAME_SIZE_SAMPLES)

                    if (readCount < 0) {
                        underruns++
                        continue
                    }

                    frameCount++

                    if (isMuted.get()) {
                        // When muted, emit zero-energy stats
                        _audioStats.value = _audioStats.value.copy(
                            currentRms = 0.0f,
                            currentDbFs = -90.0f,
                            isClipping = false,
                            framesCapturedCount = frameCount,
                            bufferUnderrunCount = underruns
                        )
                        _waveformFlow.value = List(32) { 0.05f }
                        continue
                    }

                    // Apply manual digital gain & soft limiter
                    val currentGain = gainMultiplier
                    var sumSquare = 0.0
                    var peakSample = 0.0f
                    var hasClipped = false

                    for (i in 0 until readCount) {
                        val originalVal = pcmShortBuffer[i] / 32768.0f // normalized -1.0 to 1.0
                        var amplifiedVal = originalVal * currentGain

                        // Soft-knee tanh limiter to prevent digital square-wave harsh clipping
                        if (abs(amplifiedVal) > 0.85f) {
                            hasClipped = abs(amplifiedVal) >= 1.0f
                            amplifiedVal = tanh(amplifiedVal.toDouble()).toFloat()
                        }

                        // Convert back to 16-bit signed PCM
                        val processedShort = (amplifiedVal.coerceIn(-1.0f, 1.0f) * 32767.0f).toInt().toShort()
                        pcmShortBuffer[i] = processedShort

                        // Convert to byte buffer (Little-Endian)
                        pcmByteBuffer[i * 2] = (processedShort.toInt() and 0xFF).toByte()
                        pcmByteBuffer[i * 2 + 1] = ((processedShort.toInt() shr 8) and 0xFF).toByte()

                        sumSquare += (amplifiedVal * amplifiedVal)
                        peakSample = max(peakSample, abs(amplifiedVal))
                    }

                    val meanSquare = sumSquare / readCount
                    val rms = sqrt(meanSquare).toFloat().coerceIn(0.0f, 1.0f)
                    val dbFs = if (rms > 0.00001f) (20.0f * log10(rms)).coerceIn(-90.0f, 0.0f) else -90.0f
                    val isSpeaking = rms > 0.04f

                    // Notify Telegram connection bridge with processed low-latency frame
                    onAudioFrameCaptured?.invoke(pcmByteBuffer, rms, isSpeaking)

                    // Update UI stats & generate synthetic multi-band spectrum visualizer
                    if (frameCount % 2 == 0L) {
                        _audioStats.value = _audioStats.value.copy(
                            currentRms = rms,
                            currentDbFs = dbFs,
                            isClipping = hasClipped,
                            framesCapturedCount = frameCount,
                            bufferUnderrunCount = underruns
                        )
                        _waveformFlow.value = generateSpectrumWaveform(rms, peakSample)
                    }
                }
            }, "TG-Voice-AudioCaptureThread").apply {
                start()
            }

            Log.i(TAG, "Audio capture pipeline successfully started in RAW mode: $isRawMode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio capture", e)
        }
    }

    private fun initHardwareEffects(sessionId: Int) {
        if (sessionId == 0) return
        try {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(sessionId)
                noiseSuppressor?.enabled = false // strictly disabled by default
            }
            if (AcousticEchoCanceler.isAvailable()) {
                acousticEchoCanceler = AcousticEchoCanceler.create(sessionId)
                acousticEchoCanceler?.enabled = false // strictly disabled by default
            }
            if (AutomaticGainControl.isAvailable()) {
                automaticGainControl = AutomaticGainControl.create(sessionId)
                automaticGainControl?.enabled = false // strictly disabled by default
            }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware audiofx initialization note: ${e.message}")
        }
    }

    private fun applyHardwareEffects() {
        try {
            noiseSuppressor?.enabled = enableHardwareNs
            acousticEchoCanceler?.enabled = enableHardwareAec
            automaticGainControl?.enabled = enableHardwareAgc
        } catch (e: Exception) {
            Log.w(TAG, "Failed to toggle hardware audiofx: ${e.message}")
        }
    }

    private fun generateSpectrumWaveform(rms: Float, peak: Float): List<Float> {
        val baseRms = (rms * 2.5f).coerceIn(0.06f, 1.0f)
        return List(32) { index ->
            val centerDist = abs(index - 16) / 16.0f
            val curve = (1.0f - centerDist * 0.45f)
            val jitter = (0.85f + 0.3f * (Math.random().toFloat()))
            (baseRms * curve * jitter).coerceIn(0.05f, 1.0f)
        }
    }

    fun stopCapture() {
        if (!isRecording.get()) return
        isRecording.set(false)
        try {
            captureThread?.join(500)
            captureThread = null
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            noiseSuppressor?.release()
            acousticEchoCanceler?.release()
            automaticGainControl?.release()
            noiseSuppressor = null
            acousticEchoCanceler = null
            automaticGainControl = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio capture", e)
        }
    }
}
