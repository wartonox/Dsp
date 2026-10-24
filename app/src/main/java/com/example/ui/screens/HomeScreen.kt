package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ViperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ViperViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    val profile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val spectrum by viewModel.spectrum.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val driverStatus by viewModel.driverStatus.collectAsStateWithLifecycle()
    val customPresets by viewModel.customPresets.collectAsStateWithLifecycle()

    var showDriverStatus by remember { mutableStateOf(false) }
    var showPresetManager by remember { mutableStateOf(false) }
    var showEqDetailSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ViPER DSP",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonCyan)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "NEON",
                                        color = DarkBackground,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                text = "High-Definition Audiophile Processor",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Driver status button
                    IconButton(
                        onClick = { showDriverStatus = true },
                        modifier = Modifier.testTag("driver_status_button")
                    ) {
                        Badge(
                            containerColor = if (profile.masterEnabled) NeonGreen else NeonRed,
                            modifier = Modifier.size(8.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Driver Status",
                            tint = if (profile.masterEnabled) NeonCyan else TextSecondary
                        )
                    }

                    // Presets manager button
                    IconButton(
                        onClick = { showPresetManager = true },
                        modifier = Modifier.testTag("preset_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Presets",
                            tint = NeonPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // 1. Device Output Selection Tabs (Headset, Speaker, Bluetooth, USB/DAC)
            item {
                DeviceSelectorTabs(
                    selectedDevice = selectedDevice,
                    onSelect = { viewModel.selectDevice(it) }
                )
            }

            // 2. Master Engine Power Banner + 32-band Spectrum Visualizer
            item {
                MasterPowerBanner(
                    profile = profile,
                    onToggleMaster = { viewModel.setMasterEnabled(it) },
                    spectrum = spectrum,
                    isPlaying = isPlaying,
                    currentTrack = currentTrack,
                    trackNames = viewModel.trackNames,
                    onTogglePlay = { viewModel.togglePlayback() },
                    onSelectTrack = { viewModel.selectTrack(it) }
                )
            }

            // 3. Master Gain & Limiter Section
            item {
                DspCardSection(
                    title = "Master Output & Limiter",
                    subtitle = "Gain: ${"%.1f".format(profile.masterGain)}dB • Pan: ${((profile.outputPan) * 100).toInt()}% • Limiter: ${"%.1f".format(profile.limiterThreshold)}dB",
                    icon = Icons.Default.VolumeUp,
                    isEnabled = profile.masterEnabled,
                    onToggle = { viewModel.setMasterEnabled(it) },
                    initiallyExpanded = true,
                    tag = "card_master_output"
                ) {
                    DspSlider(
                        label = "Master Output Gain",
                        value = profile.masterGain,
                        onValueChange = { viewModel.setMasterGain((it * 10).toInt() / 10f) },
                        valueRange = -30.0f..6.0f,
                        unit = " dB"
                    )
                    DspSlider(
                        label = "Output Pan (Balance)",
                        value = profile.outputPan,
                        onValueChange = { viewModel.setOutputPan((it * 100).toInt() / 100f) },
                        valueRange = -1.0f..1.0f,
                        valueFormatter = {
                            when {
                                it < -0.05f -> "${((-it) * 100).toInt()}% Left"
                                it > 0.05f -> "${(it * 100).toInt()}% Right"
                                else -> "Center"
                            }
                        }
                    )
                    DspSlider(
                        label = "Limiter Threshold",
                        value = profile.limiterThreshold,
                        onValueChange = { viewModel.setLimiterThreshold((it * 10).toInt() / 10f) },
                        valueRange = -12.0f..0.0f,
                        unit = " dB"
                    )
                }
            }

            // 4. Multi-Band Equalizer Preview & Quick Launcher
            item {
                DspCardSection(
                    title = "FIR Graphic Equalizer",
                    subtitle = "Preset: ${profile.eqPresetName} • 10 Bands Linear Phase",
                    icon = Icons.Default.GraphicEq,
                    isEnabled = profile.eqEnabled,
                    onToggle = { viewModel.setEqEnabled(it) },
                    initiallyExpanded = true,
                    tag = "card_equalizer"
                ) {
                    EqCurveVisualizer(
                        bands = profile.eqBands,
                        onBandChange = { idx, gain -> viewModel.setEqBand(idx, gain) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preset: ${profile.eqPresetName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedButton(
                            onClick = { showEqDetailSheet = true },
                            modifier = Modifier.testTag("open_eq_editor"),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("10-Band Sliders", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 5. ViPER Dynamic Bass
            item {
                DspCardSection(
                    title = "ViPER Bass (Dynamic Bass)",
                    subtitle = "Mode: ${profile.bassMode} • Cutoff: ${profile.bassFrequency}Hz • Boost: +${"%.1f".format(profile.bassGain)}dB",
                    icon = Icons.Default.SpeakerGroup,
                    isEnabled = profile.bassEnabled,
                    onToggle = { viewModel.setBassEnabled(it) },
                    tag = "card_viper_bass"
                ) {
                    Text(
                        text = "Bass Algorithm Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("Natural Bass", "Pure Bass", "Sub-Woofer"),
                        selectedOption = profile.bassMode,
                        onOptionSelected = { viewModel.setBassMode(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Bass Cutoff Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("30Hz", "40Hz", "60Hz", "80Hz", "100Hz"),
                        selectedOption = "${profile.bassFrequency}Hz",
                        onOptionSelected = {
                            val f = it.replace("Hz", "").toIntOrNull() ?: 60
                            viewModel.setBassFrequency(f)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DspSlider(
                        label = "Bass Boost Gain",
                        value = profile.bassGain,
                        onValueChange = { viewModel.setBassGain((it * 10).toInt() / 10f) },
                        valueRange = 0.0f..14.0f,
                        unit = " dB"
                    )
                }
            }

            // 6. ViPER Clarity (Treble Exciter)
            item {
                DspCardSection(
                    title = "ViPER Clarity (High-End)",
                    subtitle = "Mode: ${profile.clarityMode} • Level: +${"%.1f".format(profile.clarityGain)}dB",
                    icon = Icons.Default.AutoAwesome,
                    isEnabled = profile.clarityEnabled,
                    onToggle = { viewModel.setClarityEnabled(it) },
                    tag = "card_viper_clarity"
                ) {
                    Text(
                        text = "Clarity Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("Natural", "O-Zone", "XHiFi"),
                        selectedOption = profile.clarityMode,
                        onOptionSelected = { viewModel.setClarityMode(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DspSlider(
                        label = "Clarity Level",
                        value = profile.clarityGain,
                        onValueChange = { viewModel.setClarityGain((it * 10).toInt() / 10f) },
                        valueRange = 0.0f..14.0f,
                        unit = " dB"
                    )
                }
            }

            // 7. ViPER-DDC (Digital Driver Correction)
            item {
                DspCardSection(
                    title = "ViPER-DDC (Driver Correction)",
                    subtitle = if (profile.ddcEnabled) profile.ddcDevice else "Bypassed",
                    icon = Icons.Default.Headphones,
                    isEnabled = profile.ddcEnabled,
                    onToggle = { viewModel.setDdcEnabled(it) },
                    tag = "card_viper_ddc"
                ) {
                    DspDropdownSelector(
                        label = "Select Headphone / IEM Profile",
                        options = PresetProfiles.DDC_DEVICES,
                        selected = profile.ddcDevice,
                        onSelect = { viewModel.setDdcDevice(it) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DDC calibrates the acoustic phase and frequency response of your specific headphone model for studio monitor flatness.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // 8. Convolver (IRS Acoustic Space Simulation)
            item {
                DspCardSection(
                    title = "Convolver (IRS Engine)",
                    subtitle = if (profile.convolverEnabled) profile.convolverIrs else "Disabled",
                    icon = Icons.Default.SurroundSound,
                    isEnabled = profile.convolverEnabled,
                    onToggle = { viewModel.setConvolverEnabled(it) },
                    tag = "card_convolver"
                ) {
                    DspDropdownSelector(
                        label = "Impulse Response Sample (IRS)",
                        options = PresetProfiles.CONVOLVER_IRS_LIST,
                        selected = profile.convolverIrs,
                        onSelect = { viewModel.setConvolverIrs(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DspSlider(
                        label = "IRS Crossfeed / Wet Mix",
                        value = profile.convolverCrossfeed,
                        onValueChange = { viewModel.setConvolverCrossfeed((it * 10).toInt() / 10f) },
                        valueRange = 0f..100f,
                        unit = " %"
                    )
                }
            }

            // 9. AnalogX (Vacuum Tube Sound)
            item {
                DspCardSection(
                    title = "AnalogX (Tube Coloration)",
                    subtitle = when (profile.analogXLevel) {
                        1 -> "Level 1: Subtle Warm Harmonics"
                        2 -> "Level 2: Class-A Triode Tube"
                        else -> "Level 3: Rich Vintage Saturation"
                    },
                    icon = Icons.Default.Radio,
                    isEnabled = profile.analogXEnabled,
                    onToggle = { viewModel.setAnalogXEnabled(it) },
                    tag = "card_analogx"
                ) {
                    Text(
                        text = "Warm Tube Coloration Strength",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("Warm (Lv 1)", "Class-A (Lv 2)", "Vintage (Lv 3)"),
                        selectedOption = when (profile.analogXLevel) {
                            1 -> "Warm (Lv 1)"
                            2 -> "Class-A (Lv 2)"
                            else -> "Vintage (Lv 3)"
                        },
                        onOptionSelected = {
                            val level = when (it) {
                                "Warm (Lv 1)" -> 1
                                "Class-A (Lv 2)" -> 2
                                else -> 3
                            }
                            viewModel.setAnalogXLevel(level)
                        }
                    )
                }
            }

            // 10. Field Surround & VHS
            item {
                DspCardSection(
                    title = "Field Surround & VHS",
                    subtitle = "Level: ${profile.surroundLevel} • Mid Image: ${profile.midImage.toInt()}%",
                    icon = Icons.Default.Waves,
                    isEnabled = profile.surroundEnabled,
                    onToggle = { viewModel.setSurroundEnabled(it) },
                    tag = "card_surround"
                ) {
                    Text(
                        text = "Surround Field Strength",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("1", "2", "3", "4", "5"),
                        selectedOption = "${profile.surroundLevel}",
                        onOptionSelected = { viewModel.setSurroundLevel(it.toIntOrNull() ?: 2) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DspSlider(
                        label = "Mid Image Spreading",
                        value = profile.midImage,
                        onValueChange = { viewModel.setMidImage((it * 10).toInt() / 10f) },
                        valueRange = 0f..100f,
                        unit = " %"
                    )
                }
            }

            // 11. Playback Gain Control (AGC)
            item {
                DspCardSection(
                    title = "Playback Gain Control (AGC)",
                    subtitle = "Strength: ${profile.agcStrength} • Max: +${"%.1f".format(profile.agcMaxGain)}dB",
                    icon = Icons.Default.Speed,
                    isEnabled = profile.agcEnabled,
                    onToggle = { viewModel.setAgcEnabled(it) },
                    tag = "card_agc"
                ) {
                    Text(
                        text = "AGC Effect Strength",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("Slight", "Moderate", "Extreme"),
                        selectedOption = profile.agcStrength,
                        onOptionSelected = { viewModel.setAgcStrength(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DspSlider(
                        label = "Max Gain",
                        value = profile.agcMaxGain,
                        onValueChange = { viewModel.setAgcMaxGain((it * 10).toInt() / 10f) },
                        valueRange = 0.0f..18.0f,
                        unit = " dB"
                    )
                    DspSlider(
                        label = "Max Output",
                        value = profile.agcMaxOutput,
                        onValueChange = { viewModel.setAgcMaxOutput((it * 10).toInt() / 10f) },
                        valueRange = -6.0f..0.0f,
                        unit = " dB"
                    )
                }
            }

            // 12. Auditory System Protection (Cure Tech+)
            item {
                DspCardSection(
                    title = "Auditory Protection (Cure Tech+)",
                    subtitle = "Fatigue Protection: ${profile.cureTechLevel}",
                    icon = Icons.Default.HealthAndSafety,
                    isEnabled = profile.cureTechEnabled,
                    onToggle = { viewModel.setCureTechEnabled(it) },
                    tag = "card_cure_tech"
                ) {
                    Text(
                        text = "Protection Strength",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DspSegmentedButtons(
                        options = listOf("Slight", "Moderate", "Extreme"),
                        selectedOption = profile.cureTechLevel,
                        onOptionSelected = { viewModel.setCureTechLevel(it) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cure Tech+ prevents listening fatigue and reduces cochlear stress during extended headphone sessions without diminishing musicality.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Dialogs
    if (showDriverStatus) {
        DriverStatusDialog(
            status = driverStatus,
            onDismiss = { showDriverStatus = false }
        )
    }

    if (showPresetManager) {
        PresetDialog(
            currentPresetName = profile.eqPresetName,
            customPresets = customPresets,
            onSelectPreset = { name, bands ->
                viewModel.setEqPreset(name, bands)
                showPresetManager = false
            },
            onSaveNewPreset = { name -> viewModel.saveCustomPreset(name) },
            onDeleteCustomPreset = { name -> viewModel.deleteCustomPreset(name) },
            onDismiss = { showPresetManager = false }
        )
    }

    if (showEqDetailSheet) {
        EqualizerDetailSheet(
            eqEnabled = profile.eqEnabled,
            onEqToggle = { viewModel.setEqEnabled(it) },
            eqPresetName = profile.eqPresetName,
            bands = profile.eqBands,
            onBandChange = { idx, gain -> viewModel.setEqBand(idx, gain) },
            onSelectPreset = { name, bands -> viewModel.setEqPreset(name, bands) },
            onResetFlat = { viewModel.resetEqFlat() },
            onDismiss = { showEqDetailSheet = false }
        )
    }
}

@Composable
private fun DeviceSelectorTabs(
    selectedDevice: OutputDevice,
    onSelect: (OutputDevice) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutputDevice.values().forEach { device ->
            val isSelected = device == selectedDevice
            val icon = when (device) {
                OutputDevice.HEADSET -> Icons.Default.Headphones
                OutputDevice.SPEAKER -> Icons.Default.Speaker
                OutputDevice.BLUETOOTH -> Icons.Default.Bluetooth
                OutputDevice.USB -> Icons.Default.Usb
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelect(device) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = device.title,
                        tint = if (isSelected) NeonCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonCyan else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MasterPowerBanner(
    profile: ViperProfile,
    onToggleMaster: (Boolean) -> Unit,
    spectrum: FloatArray,
    isPlaying: Boolean,
    currentTrack: Int,
    trackNames: List<String>,
    onTogglePlay: () -> Unit,
    onSelectTrack: (Int) -> Unit
) {
    var showTrackMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (profile.masterEnabled) NeonCyan.copy(alpha = 0.45f) else DarkBorder,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Master Switch & Engine Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (profile.masterEnabled) NeonGreen else NeonRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (profile.masterEnabled) "MASTER DSP ACTIVE" else "DSP BYPASSED",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (profile.masterEnabled) NeonCyan else TextSecondary
                        )
                        Text(
                            text = "ARM64 NEON Engine • 32-bit Float Pipeline",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = profile.masterEnabled,
                    onCheckedChange = onToggleMaster,
                    modifier = Modifier.testTag("master_power_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonCyan,
                        checkedTrackColor = NeonCyan.copy(alpha = 0.35f),
                        checkedBorderColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-time 32-band spectrum
            SpectrumVisualizer(
                spectrum = spectrum,
                isPlaying = isPlaying && profile.masterEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Built-in Demo Audio Player Controls for Live Testing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DarkBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTrackMenu = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Test Track Audio Generator",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                            Text(
                                text = trackNames.getOrElse(currentTrack) { "Select Track" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Track",
                            tint = NeonCyan
                        )
                    }

                    DropdownMenu(
                        expanded = showTrackMenu,
                        onDismissRequest = { showTrackMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        trackNames.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = name,
                                        color = if (index == currentTrack) NeonCyan else TextPrimary,
                                        fontWeight = if (index == currentTrack) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectTrack(index)
                                    showTrackMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
