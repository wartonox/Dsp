package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.OutputDevice
import com.example.model.PresetProfiles
import com.example.model.ViperProfile
import org.json.JSONArray
import org.json.JSONObject

class ProfileRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("viper_dsp_prefs", Context.MODE_PRIVATE)

    fun getProfileForDevice(device: OutputDevice): ViperProfile {
        val key = "profile_${device.name}"
        val json = prefs.getString(key, null)
        return if (json != null) {
            deserializeProfile(json)
        } else {
            // Default tailored for device
            when (device) {
                OutputDevice.HEADSET -> ViperProfile(name = "Headset Default", ddcDevice = "Sennheiser HD 650", clarityEnabled = true, bassEnabled = true)
                OutputDevice.SPEAKER -> ViperProfile(name = "Speaker Calibrated", clarityEnabled = true, clarityGain = 8.0f, bassGain = 4.0f, analogXEnabled = true)
                OutputDevice.BLUETOOTH -> ViperProfile(name = "Bluetooth LDAC/aptX", clarityEnabled = true, bassGain = 7.0f, surroundEnabled = true)
                OutputDevice.USB -> ViperProfile(name = "USB DAC Hi-Res", eqPresetName = "Audiophile Acoustic", eqBands = PresetProfiles.BUILTIN_PRESETS["Audiophile Acoustic"] ?: ViperProfile().eqBands)
            }
        }
    }

    fun saveProfileForDevice(device: OutputDevice, profile: ViperProfile) {
        val key = "profile_${device.name}"
        prefs.edit().putString(key, serializeProfile(profile)).apply()
    }

    fun getCustomPresets(): Map<String, List<Float>> {
        val json = prefs.getString("custom_presets", null) ?: return emptyMap()
        val map = mutableMapOf<String, List<Float>>()
        try {
            val root = JSONObject(json)
            val keys = root.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = root.getJSONArray(k)
                val list = mutableListOf<Float>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getDouble(i).toFloat())
                }
                map[k] = list
            }
        } catch (_: Exception) {}
        return map
    }

    fun saveCustomPreset(name: String, bands: List<Float>) {
        val current = getCustomPresets().toMutableMap()
        current[name] = bands
        val root = JSONObject()
        for ((k, v) in current) {
            val arr = JSONArray()
            v.forEach { arr.put(it.toDouble()) }
            root.put(k, arr)
        }
        prefs.edit().putString("custom_presets", root.toString()).apply()
    }

    fun deleteCustomPreset(name: String) {
        val current = getCustomPresets().toMutableMap()
        current.remove(name)
        val root = JSONObject()
        for ((k, v) in current) {
            val arr = JSONArray()
            v.forEach { arr.put(it.toDouble()) }
            root.put(k, arr)
        }
        prefs.edit().putString("custom_presets", root.toString()).apply()
    }

    private fun serializeProfile(p: ViperProfile): String {
        val obj = JSONObject()
        obj.put("name", p.name)
        obj.put("masterEnabled", p.masterEnabled)
        obj.put("masterGain", p.masterGain.toDouble())
        obj.put("outputPan", p.outputPan.toDouble())
        obj.put("limiterThreshold", p.limiterThreshold.toDouble())

        obj.put("agcEnabled", p.agcEnabled)
        obj.put("agcStrength", p.agcStrength)
        obj.put("agcMaxGain", p.agcMaxGain.toDouble())
        obj.put("agcMaxOutput", p.agcMaxOutput.toDouble())

        obj.put("ddcEnabled", p.ddcEnabled)
        obj.put("ddcDevice", p.ddcDevice)

        obj.put("clarityEnabled", p.clarityEnabled)
        obj.put("clarityMode", p.clarityMode)
        obj.put("clarityGain", p.clarityGain.toDouble())

        obj.put("bassEnabled", p.bassEnabled)
        obj.put("bassMode", p.bassMode)
        obj.put("bassFrequency", p.bassFrequency)
        obj.put("bassGain", p.bassGain.toDouble())

        obj.put("eqEnabled", p.eqEnabled)
        obj.put("eqPresetName", p.eqPresetName)
        val arr = JSONArray()
        p.eqBands.forEach { arr.put(it.toDouble()) }
        obj.put("eqBands", arr)

        obj.put("convolverEnabled", p.convolverEnabled)
        obj.put("convolverIrs", p.convolverIrs)
        obj.put("convolverCrossfeed", p.convolverCrossfeed.toDouble())

        obj.put("surroundEnabled", p.surroundEnabled)
        obj.put("surroundLevel", p.surroundLevel)
        obj.put("midImage", p.midImage.toDouble())

        obj.put("analogXEnabled", p.analogXEnabled)
        obj.put("analogXLevel", p.analogXLevel)

        obj.put("cureTechEnabled", p.cureTechEnabled)
        obj.put("cureTechLevel", p.cureTechLevel)

        return obj.toString()
    }

    private fun deserializeProfile(json: String): ViperProfile {
        return try {
            val obj = JSONObject(json)
            val bands = mutableListOf<Float>()
            if (obj.has("eqBands")) {
                val arr = obj.getJSONArray("eqBands")
                for (i in 0 until arr.length()) {
                    bands.add(arr.getDouble(i).toFloat())
                }
            } else {
                bands.addAll(ViperProfile().eqBands)
            }

            ViperProfile(
                name = obj.optString("name", "Default Profile"),
                masterEnabled = obj.optBoolean("masterEnabled", true),
                masterGain = obj.optDouble("masterGain", 0.0).toFloat(),
                outputPan = obj.optDouble("outputPan", 0.0).toFloat(),
                limiterThreshold = obj.optDouble("limiterThreshold", -0.5).toFloat(),
                agcEnabled = obj.optBoolean("agcEnabled", false),
                agcStrength = obj.optString("agcStrength", "Moderate"),
                agcMaxGain = obj.optDouble("agcMaxGain", 6.0).toFloat(),
                agcMaxOutput = obj.optDouble("agcMaxOutput", -1.9).toFloat(),
                ddcEnabled = obj.optBoolean("ddcEnabled", false),
                ddcDevice = obj.optString("ddcDevice", "Sennheiser HD 650"),
                clarityEnabled = obj.optBoolean("clarityEnabled", true),
                clarityMode = obj.optString("clarityMode", "Natural"),
                clarityGain = obj.optDouble("clarityGain", 6.0).toFloat(),
                bassEnabled = obj.optBoolean("bassEnabled", true),
                bassMode = obj.optString("bassMode", "Natural Bass"),
                bassFrequency = obj.optInt("bassFrequency", 60),
                bassGain = obj.optDouble("bassGain", 6.0).toFloat(),
                eqEnabled = obj.optBoolean("eqEnabled", true),
                eqPresetName = obj.optString("eqPresetName", "ViPER Signature"),
                eqBands = if (bands.size == 10) bands else ViperProfile().eqBands,
                convolverEnabled = obj.optBoolean("convolverEnabled", false),
                convolverIrs = obj.optString("convolverIrs", "Tube Amp Warmth (Mullard 12AX7)"),
                convolverCrossfeed = obj.optDouble("convolverCrossfeed", 40.0).toFloat(),
                surroundEnabled = obj.optBoolean("surroundEnabled", false),
                surroundLevel = obj.optInt("surroundLevel", 2),
                midImage = obj.optDouble("midImage", 50.0).toFloat(),
                analogXEnabled = obj.optBoolean("analogXEnabled", true),
                analogXLevel = obj.optInt("analogXLevel", 1),
                cureTechEnabled = obj.optBoolean("cureTechEnabled", false),
                cureTechLevel = obj.optString("cureTechLevel", "Moderate")
            )
        } catch (_: Exception) {
            ViperProfile()
        }
    }
}
