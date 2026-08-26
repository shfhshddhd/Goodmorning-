package com.example.telegram.tgcalls

import android.util.Log

/**
 * JNI Bridge interface to Telegram's native C++ WebRTC Group Call engine (libtgcalls / libtgvoip).
 *
 * Implements native JNI bindings for Telegram Group Calls:
 * - Direct Opus packetization & encrypted RTP transport
 * - Hardware PCM audio frame injection from AudioEngine
 * - Remote participant PCM audio stream extraction
 * - SSRC & WebRTC SDP negotiation payload generator
 */
object TgCallsNative {

    private const val TAG = "TgCallsNative"
    
    // Status flags indicating whether compiled native shared libraries are linked and loaded
    var isTgCallsBridgeLoaded: Boolean = false
        private set

    var isTgCallsLoaded: Boolean = false
        private set

    var isTdLibLoaded: Boolean = false
        private set

    val isNativeLibraryLoaded: Boolean
        get() = isTgCallsBridgeLoaded || isTgCallsLoaded

    var nativeLoadError: String? = null
        private set

    init {
        // Attempt loading libc++_shared first if dynamically linked
        try {
            System.loadLibrary("c++_shared")
        } catch (e: Throwable) {
            // May be statically linked or system provided
        }

        // 1. Check TDLib (libtdjni.so)
        try {
            System.loadLibrary("tdjni")
            isTdLibLoaded = true
            Log.i(TAG, "Successfully loaded libtdjni.so")
        } catch (e: Throwable) {
            isTdLibLoaded = false
            Log.w(TAG, "libtdjni.so not present in jniLibs: ${e.message}")
        }

        // 2. Check tgcalls (libtgcalls.so)
        try {
            System.loadLibrary("tgcalls")
            isTgCallsLoaded = true
            Log.i(TAG, "Successfully loaded libtgcalls.so")
        } catch (e: Throwable) {
            isTgCallsLoaded = false
            Log.w(TAG, "libtgcalls.so not present in jniLibs: ${e.message}")
        }

        // 3. Check JNI bridge (libtgcalls_bridge.so)
        try {
            System.loadLibrary("tgcalls_bridge")
            isTgCallsBridgeLoaded = true
            Log.i(TAG, "Successfully loaded libtgcalls_bridge.so")
        } catch (e: Throwable) {
            isTgCallsBridgeLoaded = false
            nativeLoadError = "Missing native binaries: [libtdjni.so, libtgcalls.so, libtgcalls_bridge.so] (ABI: arm64-v8a). Place in app/src/main/jniLibs/arm64-v8a/."
            Log.w(TAG, nativeLoadError ?: "Native library not loaded")
        }
    }

    /**
     * Creates a new tgcalls group call native session.
     * @param sampleRate audio sampling rate (48000 Hz)
     * @param channels channel count (1 for Mono)
     * @return pointer to native TgCallsInstance or 0L if failed
     */
    external fun nativeCreateInstance(sampleRate: Int, channels: Int): Long

    /**
     * Configures the group call WebRTC / MTProto join payload (SSRC, SDP parameters).
     * @param instancePtr native pointer
     * @param jsonPayload Telegram joinGroupCall JSON configuration
     */
    external fun nativeSetJoinPayload(instancePtr: Long, jsonPayload: String): Boolean

    /**
     * Connects the media transport to Telegram group call reflector servers.
     * @param instancePtr native pointer
     * @param connectionJson connection endpoints & crypto keys
     */
    external fun nativeConnectTransport(instancePtr: Long, connectionJson: String): Boolean

    /**
     * Sends 20ms raw PCM audio frame (1920 bytes) to native Opus encoder & RTP sender.
     * @param instancePtr native pointer
     * @param pcmData 16-bit Mono 48kHz PCM byte array
     * @param length number of bytes in frame
     * @return number of bytes queued or -1 on error
     */
    external fun nativeSendAudioFrame(instancePtr: Long, pcmData: ByteArray, length: Int): Int

    /**
     * Updates manual digital gain factor directly in native audio processor.
     */
    external fun nativeSetGain(instancePtr: Long, gain: Float)

    /**
     * Toggles microphone mute state at native transport layer.
     */
    external fun nativeSetMuted(instancePtr: Long, muted: Boolean)

    /**
     * Queries whether WebRTC encrypted media transport is active.
     */
    external fun nativeIsConnected(instancePtr: Long): Boolean

    /**
     * Retrieves transmitted audio statistics from native layer.
     */
    external fun nativeGetTransmittedFrames(instancePtr: Long): Long

    /**
     * Pulls decoded remote audio PCM frames for playback via AudioTrack.
     * @param instancePtr native pointer
     * @param outBuffer destination byte array
     * @param maxBytes buffer capacity
     * @return number of bytes read
     */
    external fun nativeReceiveRemoteAudio(instancePtr: Long, outBuffer: ByteArray, maxBytes: Int): Int

    /**
     * Stops and releases native tgcalls session resources.
     */
    external fun nativeDestroyInstance(instancePtr: Long)
}
