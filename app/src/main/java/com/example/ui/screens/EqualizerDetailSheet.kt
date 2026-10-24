package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqFrequencies
import com.example.model.PresetProfiles
import com.example.ui.components.EqCurveVisualizer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDetailSheet(
    eqEnabled: Boolean,
    onEqToggle: (Boolean) -> Unit,
    eqPresetName: String,
    bands: List<Float>,
    onBandChange: (Int, Float) -> Unit,
    onSelectPreset: (String, List<Float>) -> Unit,
    onResetFlat: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "10-BAND GRAPHIC EQUALIZER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Preset: $eqPresetName",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onResetFlat, modifier = Modifier.testTag("reset_eq_button")) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Flat",
                            tint = TextSecondary
                        )
                    }
                    Switch(
                        checked = eqEnabled,
                        onCheckedChange = onEqToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                            checkedBorderColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-time Spline EQ Curve Visualizer
            EqCurveVisualizer(
                bands = bands,
                onBandChange = onBandChange,
                isInteractive = eqEnabled
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Horizontal Scroll Chips
            Text(
                text = "FACTORY PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetProfiles.BUILTIN_PRESETS.forEach { (name, presetBands) ->
                    val isSelected = eqPresetName == name
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(name, presetBands) },
                        label = { Text(name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else DarkBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Individual 10 Sliders with exact frequency labels and values
            Text(
                text = "FREQUENCY BANDS (±12 dB)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            EqFrequencies.labels.forEachIndexed { index, label ->
                val gain = bands.getOrElse(index) { 0f }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (eqEnabled) TextPrimary else TextMuted,
                        modifier = Modifier.width(52.dp)
                    )

                    Slider(
                        value = gain,
                        onValueChange = { newG ->
                            onBandChange(index, (newG * 10).toInt() / 10f)
                        },
                        valueRange = -12.0f..12.0f,
                        enabled = eqEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slider_band_$index"),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = DarkSurfaceVariant,
                            disabledThumbColor = TextMuted,
                            disabledActiveTrackColor = DarkBorder,
                            disabledInactiveTrackColor = DarkSurfaceVariant
                        )
                    )

                    Text(
                        text = "${if (gain > 0) "+" else ""}${"%.1f".format(gain)} dB",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (gain > 0) NeonCyan else if (gain < 0) NeonOrange else TextSecondary,
                        modifier = Modifier.width(56.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
