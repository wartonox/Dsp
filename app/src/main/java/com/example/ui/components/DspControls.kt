package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DspCardSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "",
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (tag.isNotEmpty()) tag else "card_${title.lowercase().replace(" ", "_")}")
            .border(
                width = 1.dp,
                color = if (isEnabled) NeonCyan.copy(alpha = 0.35f) else DarkBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) DarkSurface else DarkSurface.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header row: Icon + Title/Subtitle + Neon Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isEnabled) NeonCyan.copy(alpha = 0.15f)
                                else DarkSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isEnabled) NeonCyan else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isEnabled) TextPrimary else TextSecondary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) NeonCyanGlow else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        onToggle(it)
                        if (it) isExpanded = true
                    },
                    modifier = Modifier.testTag("switch_${title.lowercase().replace(" ", "_")}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                        checkedBorderColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant,
                        uncheckedBorderColor = DarkBorder
                    )
                )
            }

            AnimatedVisibility(
                visible = isEnabled && isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(
                        color = DarkBorder.copy(alpha = 0.6f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun DspSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String = { "%.1f".format(it) },
    modifier: Modifier = Modifier,
    unit: String = "",
    tag: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "${valueFormatter(value)}$unit",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(if (tag.isNotEmpty()) tag else "slider_${label.lowercase().replace(" ", "_")}"),
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
fun DspSegmentedButtons(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) NeonCyan else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DspDropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                options.forEach { option ->
                    val isChosen = option == selected
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    color = if (isChosen) NeonCyan else TextPrimary,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
