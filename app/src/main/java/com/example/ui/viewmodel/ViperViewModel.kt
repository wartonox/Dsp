package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ViperDspEngine
import com.example.data.ProfileRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application.applicationContext)
    val dspEngine = ViperDspEngine(viewModelScope)

    private val _selectedDevice = MutableStateFlow(OutputDevice.HEADSET)
    val selectedDevice = _selectedDevice.asStateFlow()

    private val _currentProfile = MutableStateFlow(ViperProfile())
    val currentProfile = _currentProfile.asStateFlow()

    private val _customPresets = MutableStateFlow<Map<String, List<Float>>>(emptyMap())
    val customPresets = _customPresets.asStateFlow()

    val isPlaying = dspEngine.isPlaying
    val spectrum = dspEngine.spectrumBins
    val currentTrack = dspEngine.currentTrackIndex
    val trackNames = dspEngine.trackNames

    private val _driverStatus = MutableStateFlow(DriverStatus())
    val driverStatus = _driverStatus.asStateFlow()

    init {
        loadDeviceProfile(OutputDevice.HEADSET)
        loadCustomPresets()
    }

    fun selectDevice(device: OutputDevice) {
        // Save current profile to previous device first
        repository.saveProfileForDevice(_selectedDevice.value, _currentProfile.value)
        _selectedDevice.value = device
        loadDeviceProfile(device)
    }

    private fun loadDeviceProfile(device: OutputDevice) {
        val profile = repository.getProfileForDevice(device)
        _currentProfile.value = profile
        dspEngine.updateProfile(profile)
        updateDriverStatus(profile)
    }

    private fun loadCustomPresets() {
        _customPresets.value = repository.getCustomPresets()
    }

    fun updateProfile(modifier: (ViperProfile) -> ViperProfile) {
        val updated = modifier(_currentProfile.value)
        _currentProfile.value = updated
        dspEngine.updateProfile(updated)
        repository.saveProfileForDevice(_selectedDevice.value, updated)
        updateDriverStatus(updated)
    }

    private fun updateDriverStatus(profile: ViperProfile) {
        _driverStatus.value = DriverStatus(
            enabled = profile.masterEnabled,
            status = if (profile.masterEnabled) "Normal (Operational)" else "Disabled (Bypassed)",
            processing = profile.masterEnabled && (
                profile.eqEnabled || profile.bassEnabled || profile.clarityEnabled ||
                profile.analogXEnabled || profile.surroundEnabled || profile.convolverEnabled
            )
        )
    }

    // Master Controls
    fun setMasterEnabled(enabled: Boolean) = updateProfile { it.copy(masterEnabled = enabled) }
    fun setMasterGain(gain: Float) = updateProfile { it.copy(masterGain = gain) }
    fun setOutputPan(pan: Float) = updateProfile { it.copy(outputPan = pan) }
    fun setLimiterThreshold(threshold: Float) = updateProfile { it.copy(limiterThreshold = threshold) }

    // AGC
    fun setAgcEnabled(enabled: Boolean) = updateProfile { it.copy(agcEnabled = enabled) }
    fun setAgcStrength(strength: String) = updateProfile { it.copy(agcStrength = strength) }
    fun setAgcMaxGain(gain: Float) = updateProfile { it.copy(agcMaxGain = gain) }
    fun setAgcMaxOutput(output: Float) = updateProfile { it.copy(agcMaxOutput = output) }

    // DDC
    fun setDdcEnabled(enabled: Boolean) = updateProfile { it.copy(ddcEnabled = enabled) }
    fun setDdcDevice(device: String) = updateProfile { it.copy(ddcDevice = device) }

    // Clarity
    fun setClarityEnabled(enabled: Boolean) = updateProfile { it.copy(clarityEnabled = enabled) }
    fun setClarityMode(mode: String) = updateProfile { it.copy(clarityMode = mode) }
    fun setClarityGain(gain: Float) = updateProfile { it.copy(clarityGain = gain) }

    // Bass
    fun setBassEnabled(enabled: Boolean) = updateProfile { it.copy(bassEnabled = enabled) }
    fun setBassMode(mode: String) = updateProfile { it.copy(bassMode = mode) }
    fun setBassFrequency(freq: Int) = updateProfile { it.copy(bassFrequency = freq) }
    fun setBassGain(gain: Float) = updateProfile { it.copy(bassGain = gain) }

    // EQ
    fun setEqEnabled(enabled: Boolean) = updateProfile { it.copy(eqEnabled = enabled) }
    fun setEqBand(index: Int, gain: Float) {
        updateProfile { profile ->
            val updatedBands = profile.eqBands.toMutableList()
            if (index in updatedBands.indices) {
                updatedBands[index] = gain
            }
            profile.copy(eqBands = updatedBands, eqPresetName = "Custom")
        }
    }
    fun setEqPreset(name: String, bands: List<Float>) {
        updateProfile { it.copy(eqPresetName = name, eqBands = bands) }
    }
    fun resetEqFlat() {
        setEqPreset("Flat (Studio Reference)", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f))
    }

    // Convolver
    fun setConvolverEnabled(enabled: Boolean) = updateProfile { it.copy(convolverEnabled = enabled) }
    fun setConvolverIrs(irs: String) = updateProfile { it.copy(convolverIrs = irs) }
    fun setConvolverCrossfeed(crossfeed: Float) = updateProfile { it.copy(convolverCrossfeed = crossfeed) }

    // Field Surround
    fun setSurroundEnabled(enabled: Boolean) = updateProfile { it.copy(surroundEnabled = enabled) }
    fun setSurroundLevel(level: Int) = updateProfile { it.copy(surroundLevel = level) }
    fun setMidImage(midImage: Float) = updateProfile { it.copy(midImage = midImage) }

    // AnalogX
    fun setAnalogXEnabled(enabled: Boolean) = updateProfile { it.copy(analogXEnabled = enabled) }
    fun setAnalogXLevel(level: Int) = updateProfile { it.copy(analogXLevel = level) }

    // Cure Tech
    fun setCureTechEnabled(enabled: Boolean) = updateProfile { it.copy(cureTechEnabled = enabled) }
    fun setCureTechLevel(level: String) = updateProfile { it.copy(cureTechLevel = level) }

    // Custom Presets
    fun saveCustomPreset(name: String) {
        repository.saveCustomPreset(name, _currentProfile.value.eqBands)
        loadCustomPresets()
        setEqPreset(name, _currentProfile.value.eqBands)
    }

    fun deleteCustomPreset(name: String) {
        repository.deleteCustomPreset(name)
        loadCustomPresets()
    }

    // Audio Engine
    fun togglePlayback() = dspEngine.togglePlayback()
    fun selectTrack(index: Int) = dspEngine.selectTrack(index)

    override fun onCleared() {
        super.onCleared()
        dspEngine.release()
    }
}
