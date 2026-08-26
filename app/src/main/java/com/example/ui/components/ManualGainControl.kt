package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDarkBorder
import com.example.ui.theme.TgDarkCard
import com.example.ui.theme.TgDarkSurfaceVariant
import com.example.ui.theme.TgTextMuted
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import com.example.ui.theme.TgVoiceGreen
import java.util.Locale
import kotlin.math.log10

@Composable
fun ManualGainControl(
    gainMultiplier: Float,
    isRawMode: Boolean,
    onGainChanged: (Float) -> Unit,
    onRawModeToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gainDb = if (gainMultiplier > 0.001f) 20.0f * log10(gainMultiplier) else -20.0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TgDarkCard)
            .border(1.dp, TgDarkBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = TgCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MANUAL MIC GAIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TgTextPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // dB & Multiplier readout badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TgDarkSurfaceVariant)
                    .border(1.dp, TgDarkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1fx (%+.1f dB)", gainMultiplier, gainDb),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (gainMultiplier > 2.0f) TgVoiceGreen else TgCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Precision Continuous Slider (0.1x to 10.0x)
        Slider(
            value = gainMultiplier,
            onValueChange = onGainChanged,
            valueRange = 0.1f..10.0f,
            steps = 98,
            colors = SliderDefaults.colors(
                thumbColor = TgCyan,
                activeTrackColor = TgCyan,
                inactiveTrackColor = Color(0xFF26354A)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manual_gain_slider")
        )

        // Quick Gain Presets Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val presets = listOf(
                0.5f to "0.5x (-6dB)",
                1.0f to "1.0x (Unity)",
                2.0f to "2.0x (+6dB)",
                4.0f to "4.0x (+12dB)",
                8.0f to "8.0x (+18dB)"
            )

            presets.forEach { (presetVal, label) ->
                val isSelected = kotlin.math.abs(gainMultiplier - presetVal) < 0.15f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) TgCyan.copy(alpha = 0.2f) else TgDarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) TgCyan else TgDarkBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onGainChanged(presetVal) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label.split(" ")[0],
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TgCyan else TgTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Raw Mode / DSP Bypass Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TgDarkSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Raw PCM Capture",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TgTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRawMode) "STRICT BYPASS" else "HARDWARE DSP",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isRawMode) TgVoiceGreen else TgTextMuted
                    )
                }
                Text(
                    text = if (isRawMode) "No AGC, No Echo Cancel, No Noise Supression (Pure Mic Input)" else "Android hardware voice enhancements active",
                    fontSize = 11.sp,
                    color = TgTextMuted,
                    lineHeight = 14.sp
                )
            }

            Switch(
                checked = isRawMode,
                onCheckedChange = onRawModeToggled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TgVoiceGreen,
                    checkedTrackColor = TgVoiceGreen.copy(alpha = 0.3f),
                    uncheckedThumbColor = TgTextSecondary,
                    uncheckedTrackColor = TgDarkBorder
                ),
                modifier = Modifier.testTag("raw_mode_switch")
            )
        }
    }
}
