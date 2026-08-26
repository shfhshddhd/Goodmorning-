# Telegram Voice Client (Standalone Export & Android Studio Setup Guide)

## 📌 Project Overview
This project is an Android Native Application built with Kotlin & Jetpack Compose specifically tailored for **Telegram Voice Chats / Live Group Calls**.
It contains:
- **Telegram Dialogs & Active Voice Chats Filtering**: Only groups with active voice calls (including calls with 0 participants) are displayed; all text messages, channels, DMs, and non-voice media are suppressed.
- **Hardware Low-Latency Raw Audio Pipeline (`AudioEngine.kt`)**: Bypasses Android hardware DSP (`AutomaticGainControl`, `NoiseSuppressor`, `AcousticEchoCanceler`) and captures uncompressed 48,000 Hz 16-bit Mono PCM (20ms frames matching Opus audio specifications).
- **Manual Gain Engine & Dynamic Limiter (`ManualGainControl.kt`)**: 0.1x ($-20\text{ dB}$) to 10.0x ($+24\text{ dB}$) manual gain slider with real-time waveform visualization, RMS metering, and soft-knee saturation protection.

---

## 🛠️ Requirements for Physical Device Build (Android Studio)
1. **Android Studio**: Ladybug / Koala or newer.
2. **Android NDK**: Version `25.x` or `26.x` (Install via *SDK Manager -> SDK Tools -> NDK (Side by side)*).
3. **CMake**: Version `3.22.1+`.
4. **Physical Android Device**: Android 7.0 (API 24) to Android 15 (API 35+), connected via USB with *USB Debugging* enabled.

---

## 🔌 Integrating Real Telegram Native Libraries (TDLib & libtgcalls)

Telegram Group Voice Calls require two native binary modules:
1. **TDLib (`libtdjni.so`)**: Handles MTProto 2.0 binary TCP connection (`auth.sendCode`, `auth.signIn`, `auth.checkPassword`, `getDialogs`, `getGroupCall`).
2. **libtgcalls (`libtgcalls.so`)**: Handles WebRTC audio transport, Opus encoding/decoding, and encrypted RTP packet streaming for Telegram voice rooms.

### Step 1: Add TDLib AAR / JNI Dependencies
Download or build pre-compiled `tdlib-android` or add via Gradle:
```kotlin
// In app/build.gradle.kts dependencies:
implementation("org.drinkless.td:tdlib:1.8.0")
```
Or place the native shared libraries in `app/src/main/jniLibs/`:
- `jniLibs/arm64-v8a/libtdjni.so`
- `jniLibs/armeabi-v7a/libtdjni.so`
- `jniLibs/x86_64/libtdjni.so`

### Step 2: Add libtgcalls for Group Voice Calls
Place pre-built `libtgcalls.so` and `libwebrtc.so` inside `app/src/main/jniLibs/` or link Telegram's official C++ voice chat engine:
- Repository: [https://github.com/Telegram-Calls/tgcalls](https://github.com/Telegram-Calls/tgcalls)

---

## 🔑 Setting Your Telegram API Credentials
1. Log in to [https://my.telegram.org](https://my.telegram.org).
2. Go to **API development tools** and generate your `api_id` and `api_hash`.
3. You can either enter them directly inside the app UI under **"▼ Custom Telegram API Credentials"** or define them in `app/build.gradle.kts` / `TelegramClientBridge.kt`:
```kotlin
const val DEFAULT_API_ID = YOUR_API_ID_HERE
const val DEFAULT_API_HASH = "YOUR_API_HASH_HERE"
```

---

## 🚀 How to Build & Install APK on Physical Device
1. Open this project folder in **Android Studio**.
2. Wait for Gradle Sync to complete.
3. Connect your Android phone via USB.
4. Click **Run 'app'** (`Shift + F10`) or run from terminal:
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```
5. Grant Microphone permission when prompted, enter your Telegram phone number with international country code (`+91...`), submit the OTP received in your Telegram app, and join any active voice chat.
