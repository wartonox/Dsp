package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PresetProfiles
import com.example.ui.theme.*

@Composable
fun PresetDialog(
    currentPresetName: String,
    customPresets: Map<String, List<Float>>,
    onSelectPreset: (name: String, bands: List<Float>) -> Unit,
    onSaveNewPreset: (name: String) -> Unit,
    onDeleteCustomPreset: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPresetName by remember { mutableStateOf("") }
    var showAddInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preset_dialog")
                .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PRESET MANAGER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { showAddInput = !showAddInput },
                        modifier = Modifier.testTag("add_preset_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save Current as Preset",
                            tint = NeonCyan
                        )
                    }
                }

                if (showAddInput) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            placeholder = { Text("Preset name...", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_preset_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPresetName.isNotBlank()) {
                                    onSaveNewPreset(newPresetName.trim())
                                    newPresetName = ""
                                    showAddInput = false
                                }
                            },
                            modifier = Modifier.testTag("save_preset_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                ) {
                    if (customPresets.isNotEmpty()) {
                        item {
                            Text(
                                text = "CUSTOM PRESETS",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        items(customPresets.entries.toList()) { (name, bands) ->
                            val isSelected = currentPresetName == name
                            PresetRow(
                                name = name,
                                isSelected = isSelected,
                                isCustom = true,
                                onSelect = { onSelectPreset(name, bands) },
                                onDelete = { onDeleteCustomPreset(name) }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "VIPER FACTORY PRESETS",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple,
                            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                        )
                    }

                    items(PresetProfiles.BUILTIN_PRESETS.entries.toList()) { (name, bands) ->
                        val isSelected = currentPresetName == name
                        PresetRow(
                            name = name,
                            isSelected = isSelected,
                            isCustom = false,
                            onSelect = { onSelectPreset(name, bands) },
                            onDelete = {}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun PresetRow(
    name: String,
    isSelected: Boolean,
    isCustom: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceVariant)
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NeonCyan else TextPrimary
            )
        }

        if (isCustom) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete preset",
                    tint = NeonRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
