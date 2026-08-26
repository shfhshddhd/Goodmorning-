package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDarkCard
import com.example.ui.theme.TgDarkSurface
import com.example.ui.theme.TgVoiceGreen
import com.example.ui.theme.TgVoiceGreenGlow
import com.example.ui.theme.TgVoiceMutedGlow
import com.example.ui.theme.TgVoiceMutedRed
import com.example.viewmodel.MicInteractionType

@Composable
fun GiantMicButton(
    isMuted: Boolean,
    isSpeaking: Boolean,
    rmsLevel: Float,
    micMode: MicInteractionType,
    onToggleMute: () -> Unit,
    onPttPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (!isMuted && isSpeaking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Dynamic audio reactive expansion
    val dynamicRmsScale = if (!isMuted) (1.0f + (rmsLevel * 0.4f).coerceIn(0.0f, 0.5f)) else 1.0f

    val ringColor by animateColorAsState(
        targetValue = when {
            isMuted -> TgVoiceMutedRed.copy(alpha = 0.25f)
            isSpeaking -> TgVoiceGreen.copy(alpha = 0.7f)
            else -> TgCyan.copy(alpha = 0.5f)
        },
        label = "ring_color"
    )

    val buttonGradient = when {
        isMuted -> Brush.radialGradient(
            colors = listOf(Color(0xFF2B1D20), TgDarkCard, Color(0xFF16151A))
        )
        isSpeaking -> Brush.radialGradient(
            colors = listOf(TgVoiceGreen, Color(0xFF15803D), Color(0xFF14532D))
        )
        else -> Brush.radialGradient(
            colors = listOf(Color(0xFF2B1F45), TgBlue, TgDarkCard)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(240.dp)
            .testTag("giant_mic_button")
    ) {
        // Outer glowing ripple rings
        if (!isMuted) {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(pulseScale * dynamicRmsScale)
                    .clip(CircleShape)
                    .background(if (isSpeaking) TgVoiceGreenGlow else TgCyan.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .size(195.dp)
                    .scale(dynamicRmsScale)
                    .clip(CircleShape)
                    .border(2.dp, ringColor, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(TgVoiceMutedGlow)
            )
        }

        // Inner solid touch button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(buttonGradient)
                .border(
                    width = 4.dp,
                    color = if (!isMuted) (if (isSpeaking) TgVoiceGreen else TgBlue) else TgVoiceMutedRed,
                    shape = CircleShape
                )
                .pointerInput(micMode) {
                    if (micMode == MicInteractionType.PUSH_TO_TALK) {
                        detectTapGestures(
                            onPress = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onPttPressChange(true)
                                tryAwaitRelease()
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onPttPressChange(false)
                            }
                        )
                    } else {
                        detectTapGestures(
                            onTap = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onToggleMute()
                            }
                        )
                    }
                }
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isMuted) "Mic Muted - Tap to speak" else "Mic Active - Tap to mute",
                tint = if (isMuted) TgVoiceMutedRed else if (isSpeaking) Color.White else TgCyan,
                modifier = Modifier.size(68.dp)
            )
        }
    }
}
