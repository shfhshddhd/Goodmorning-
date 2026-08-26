# Telegram Voice Client (Standalone Export & Native Dependency Guide)

## 📌 Project Architecture & Physical Device Readiness

This project is an Android Native Application built with Kotlin & Jetpack Compose specifically tailored for **Telegram Voice Chats / Live Group Calls**.

---

## 🔍 Native Subsystem Status & Exact Requirements

| Subsystem / Layer | Component | Implementation Status | Where Provided / Missing Action |
| :--- | :--- | :--- | :--- |
| **Android UI & Navigation** | Jetpack Compose (M3) | **COMPLETE & READY** | `app/src/main/java/com/example/ui/` |
| **Hardware Microphone Capture** | `AudioEngine.kt` (AudioRecord) | **COMPLETE & READY** | Captures uncompressed 48 kHz 16-bit Mono PCM (20ms frames = 1920 bytes) bypassing OS-level AGC/NS/AEC |
| **Manual Gain Engine** | `ManualGainControl.kt` | **COMPLETE & READY** | 0.1x to 10.0x manual slider with soft-knee cubic saturation limiter and clipping warning |
| **Diagnostics Subsystem** | `DiagnosticScreen.kt` | **COMPLETE & READY** | Live 11-point subsystem verification inspector |
| **Foreground Service** | `VoiceChatForegroundService.kt`| **COMPLETE & READY** | Holds Android OS microphone wake-lock during background calls |
| **JNI Audio/RTP Adapter** | `tgcalls_bridge.cpp` & `TgCallsNative.kt` | **COMPLETE & READY** | C++ JNI bridge exported for dynamic linking with tgcalls |
| **MTProto 2.0 Client (TDLib)**| `libtdjni.so` (ARM64) | **MISSING EXTERNAL BINARY** | Must be placed in `app/src/main/jniLibs/arm64-v8a/libtdjni.so` |
| **Telegram Voice Reflector (tgcalls)**| `libtgcalls.so` (ARM64) | **MISSING EXTERNAL BINARY** | Must be placed in `app/src/main/jniLibs/arm64-v8a/libtgcalls.so` |

---

## 📁 Exact Directory Structure for Missing Native Binaries

To perform a **REAL Telegram Group Voice Chat test on an ARM64 physical Android phone**, place the compiled native shared libraries into the following directories:

```text
app/
└── src/
    └── main/
        ├── cpp/
        │   ├── CMakeLists.txt
        │   └── tgcalls_bridge.cpp
        └── jniLibs/
            └── arm64-v8a/
                ├── libtdjni.so      <-- [REQUIRED] Telegram MTProto 2.0 TDLib binary
                └── libtgcalls.so    <-- [REQUIRED] Telegram WebRTC Voice Call engine binary
```

*(Optional for 32-bit ARM or x86_64 devices: also populate `jniLibs/armeabi-v7a/` and `jniLibs/x86_64/`)*

---

## 📥 How to Obtain the Missing Binaries

### Option 1: Using Pre-compiled TDLib & tgcalls AAR / Binaries
1. **TDLib (`libtdjni.so`)**:
   - Official Repository: [https://github.com/tdlib/td](https://github.com/tdlib/td)
   - Or download prebuilt Android TDLib releases from: [https://github.com/egor-xyz/tdlib-binaries](https://github.com/egor-xyz/tdlib-binaries)
   - Copy `libtdjni.so` for `arm64-v8a` into `app/src/main/jniLibs/arm64-v8a/`.

2. **tgcalls (`libtgcalls.so`)**:
   - Official Telegram Calls Repository: [https://github.com/Telegram-Calls/tgcalls](https://github.com/Telegram-Calls/tgcalls)
   - Official Telegram Android App Source: [https://github.com/DrKLO/Telegram/tree/master/TMessagesProj/jni/tgcalls](https://github.com/DrKLO/Telegram/tree/master/TMessagesProj/jni/tgcalls)
   - Copy `libtgcalls.so` into `app/src/main/jniLibs/arm64-v8a/`.

### Option 2: Compiling TDLib from Source with NDK
```bash
# Clone TDLib
git clone https://github.com/tdlib/td.git
cd td
mkdir build-android-arm64 && cd build-android-arm64

# Configure with Android NDK toolchain
cmake -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_ROOT/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI=arm64-v8a \
      -DANDROID_PLATFORM=android-24 \
      -DCMAKE_BUILD_TYPE=Release \
      -DTD_ENABLE_JNI=ON ..

cmake --build . --target tdjni -j$(nproc)
cp libtdjni.so /path/to/app/src/main/jniLibs/arm64-v8a/
```

---

## ⚙️ Native Library Loading Order & Dynamic Linkage

At runtime on Android (`arm64-v8a`), the application initializes libraries in the following strict order:

1. **`libc++_shared.so`** *(Optional if linked dynamically by NDK)*: Standard C++ library runtime.
2. **`libtdjni.so`**: TDLib MTProto 2.0 client engine for Telegram TCP transport, login, and group call signaling.
3. **`libtgcalls.so`**: Upstream Telegram WebRTC / Opus / SRTP group call engine.
4. **`libtgcalls_bridge.so`**: Project JNI bridge routing 48 kHz PCM frames from `AudioRecord` to `libtgcalls.so`.

---

## 🚀 Step-by-Step Android Studio & NDK Setup Procedure

### Step 1: Export Project & Open in Android Studio
1. Export/Download the project as a ZIP archive.
2. Unzip and open the root folder in **Android Studio** (Koala / Ladybug or newer).

### Step 2: Install NDK & CMake
1. In Android Studio, go to **Settings (Preferences) -> Appearance & Behavior -> System Settings -> Android SDK -> SDK Tools**.
2. Check and install:
   - **NDK (Side by side)** (Recommended: `25.2.9519653` or `26.3.11579264`).
   - **CMake** (`3.22.1` or higher).

### Step 3: Populate `jniLibs/arm64-v8a/`
Create the folder `app/src/main/jniLibs/arm64-v8a/` and paste the two compiled native libraries:
- `app/src/main/jniLibs/arm64-v8a/libtdjni.so`
- `app/src/main/jniLibs/arm64-v8a/libtgcalls.so`

### Step 4: Build and Deploy to Physical Device
1. Connect your physical Android phone (ARM64) via USB with **USB Debugging** enabled.
2. In Android Studio terminal or system terminal, run:
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 5: Verify via Diagnostics Inspector
1. Launch the app on your phone.
2. Tap the **Speedometer (Diagnostics)** icon on the top right.
3. Verify that:
   - **Check #4 (Native Libraries Loaded)** shows **PASS**.
   - **Check #1 (Telegram Authentication)** shows **PASS**.
   - **Check #10 & #11 (Transmission)** show **PASS** once connected to a live room.

