package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SpectrumVisualizer(
    spectrum: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val peakHolds = remember { FloatArray(32) { 0f } }

    LaunchedEffect(spectrum) {
        for (i in spectrum.indices) {
            val target = if (isPlaying) spectrum.getOrElse(i) { 0f } else 0.05f
            if (target > peakHolds[i]) {
                peakHolds[i] = target
            } else {
                peakHolds[i] = (peakHolds[i] * 0.92f).coerceAtLeast(0.04f)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPlaying) NeonGreen else TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "DSP ENGINE ACTIVE • 32-BAND FFT" else "DSP ENGINE STANDBY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPlaying) NeonGreen else TextSecondary,
                    letterSpacing = 1.1.sp
                )
            }
            Text(
                text = "96kHz / 32-bit",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBackground)
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val binCount = 32
                val barSpacing = 2.dp.toPx()
                val totalSpacing = barSpacing * (binCount - 1)
                val barWidth = ((size.width - totalSpacing) / binCount).coerceAtLeast(1f)
                val canvasH = size.height

                for (i in 0 until binCount) {
                    val rawMag = if (isPlaying) spectrum.getOrElse(i) { 0.05f } else 0.04f
                    val mag = rawMag.coerceIn(0.04f, 1.0f)
                    val barH = canvasH * mag
                    val x = i * (barWidth + barSpacing)
                    val y = canvasH - barH

                    // Draw Gradient Frequency Bar
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NeonViolet,
                                NeonCyan,
                                NeonCyanGlow
                            ),
                            startY = y,
                            endY = canvasH
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Draw Peak Hold Needle
                    val peakH = (canvasH * peakHolds[i].coerceIn(0.04f, 1f))
                    val peakY = (canvasH - peakH).coerceAtLeast(1f)
                    drawRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(x, peakY),
                        size = Size(barWidth, 1.5.dp.toPx())
                    )
                }
            }
        }
    }
}
