package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DriverStatus
import com.example.ui.theme.*

@Composable
fun DriverStatusDialog(
    status: DriverStatus,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("driver_status_dialog")
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ViPER DSP DRIVER STATUS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Hardware Acceleration & Engine Telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Status item rows
                DriverStatusRow("Driver Version", status.version, NeonCyan)
                DriverStatusRow("NEON Enabled", if (status.neonEnabled) "Yes" else "No", NeonGreen)
                DriverStatusRow("Enabled", if (status.enabled) "Yes" else "No", if (status.enabled) NeonGreen else NeonRed)
                DriverStatusRow("Status", status.status, NeonGreen)
                DriverStatusRow("Audio Format", status.audioFormat, TextPrimary)
                DriverStatusRow("Processing", if (status.processing) "Yes (Active)" else "No", if (status.processing) NeonCyan else TextMuted)
                DriverStatusRow("Sampling Rate", status.samplingRate, TextPrimary)
                DriverStatusRow("Buffer Latency", status.latency, NeonCyanGlow)
                DriverStatusRow("Architecture", "ARM64-v8a NEON High-Precision", TextSecondary)
                DriverStatusRow("Effect UUID", "48e89bc0-0c37-11e1-88fe-0002a5d5c51b", TextMuted, isMonospace = true)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_status_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Close",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverStatusRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            fontSize = if (isMonospace) 9.sp else 12.sp
        )
    }
}
