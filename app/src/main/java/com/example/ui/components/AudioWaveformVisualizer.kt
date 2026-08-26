package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDarkBorder
import com.example.ui.theme.TgDarkCard
import com.example.ui.theme.TgTextMuted
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import com.example.ui.theme.TgVoiceGreen
import com.example.ui.theme.TgVoiceMutedRed
import com.example.ui.theme.WaveformPeakRed
import java.util.Locale

@Composable
fun AudioWaveformVisualizer(
    waveform: List<Float>,
    currentDbFs: Float,
    isMuted: Boolean,
    isClipping: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TgDarkCard)
            .border(1.dp, TgDarkBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top header: Live dB Meter & Limiter Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MIC INPUT LEVEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TgTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Live dBFS readout
                Text(
                    text = if (isMuted) "MUTED" else String.format(Locale.US, "%.1f dBFS", currentDbFs),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMuted) TgVoiceMutedRed else if (currentDbFs > -6.0f) WaveformPeakRed else TgVoiceGreen
                )
            }

            // Soft-knee Limiter / Peak Status Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isClipping) "LIMITER ENGAGED" else "RAW 48kHz",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isClipping) WaveformPeakRed else TgTextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMuted) Color.Gray
                            else if (isClipping) WaveformPeakRed
                            else TgVoiceGreen
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic 32-band Audio Frequency / RMS Waveform Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            val barCount = waveform.size.coerceAtLeast(1)
            val totalWidth = size.width
            val totalHeight = size.height
            val spacing = 3.dp.toPx()
            val availableWidthForBars = totalWidth - (spacing * (barCount - 1))
            val barWidth = (availableWidthForBars / barCount).coerceAtLeast(2.dp.toPx())

            val centerY = totalHeight / 2.0f

            for (i in 0 until barCount) {
                val rawAmp = if (isMuted) 0.05f else waveform.getOrElse(i) { 0.05f }
                val barHeight = (rawAmp * totalHeight).coerceIn(4.dp.toPx(), totalHeight)

                val left = i * (barWidth + spacing)
                val top = centerY - (barHeight / 2.0f)

                val barBrush = if (isMuted) {
                    Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                } else if (rawAmp > 0.8f) {
                    Brush.verticalGradient(listOf(WaveformPeakRed, TgCyan))
                } else {
                    Brush.verticalGradient(listOf(TgCyan, TgVoiceGreen))
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Linear dB scale meter bar (-60dB to 0dB)
        val normalizedLevel = if (isMuted) 0.0f else ((currentDbFs + 60.0f) / 60.0f).coerceIn(0.0f, 1.0f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = normalizedLevel)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(TgVoiceGreen, TgCyan, WaveformPeakRed)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("-60 dB", fontSize = 9.sp, color = TgTextMuted, fontFamily = FontFamily.Monospace)
            Text("-30 dB", fontSize = 9.sp, color = TgTextMuted, fontFamily = FontFamily.Monospace)
            Text("-12 dB", fontSize = 9.sp, color = TgTextMuted, fontFamily = FontFamily.Monospace)
            Text("0 dB (Peak)", fontSize = 9.sp, color = TgTextMuted, fontFamily = FontFamily.Monospace)
        }
    }
}
