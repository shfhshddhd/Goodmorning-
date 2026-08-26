package com.example.audio

data class AudioStats(
    val sampleRate: Int = 48000,
    val channelCount: Int = 1,
    val frameSizeSamples: Int = 960, // 20ms at 48kHz
    val frameDurationMs: Float = 20.0f,
    val minBufferSize: Int = 1920,
    val hardwareLatencyEstimateMs: Float = 14.5f,
    val gainMultiplier: Float = 1.0f,
    val currentRms: Float = 0.0f, // 0.0 to 1.0
    val currentDbFs: Float = -90.0f, // -90dB to 0dB
    val isClipping: Boolean = false,
    val rawModeEnabled: Boolean = true, // strictly raw by default
    val hardwareAgcActive: Boolean = false,
    val hardwareNsActive: Boolean = false,
    val hardwareAecActive: Boolean = false,
    val framesCapturedCount: Long = 0L,
    val bufferUnderrunCount: Int = 0
)
