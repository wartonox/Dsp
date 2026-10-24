package com.example.model

enum class OutputDevice(val title: String, val subtitle: String) {
    HEADSET("Headset", "Wired / In-Ear"),
    SPEAKER("Speaker", "Internal Loudspeaker"),
    BLUETOOTH("Bluetooth", "A2DP / LDAC / aptX"),
    USB("USB / DAC", "Hi-Res Audio DAC")
}

data class ViperProfile(
    val name: String = "Default Profile",
    // Master Power & Gain
    val masterEnabled: Boolean = true,
    val masterGain: Float = 0f, // -60dB to +6dB
    val outputPan: Float = 0f, // -1f (Full Left) to 1f (Full Right)
    val limiterThreshold: Float = -0.5f, // -12dB to 0dB

    // Playback Gain Control (AGC)
    val agcEnabled: Boolean = false,
    val agcStrength: String = "Moderate", // Slight, Moderate, Extreme
    val agcMaxGain: Float = 6.0f, // 0 to 18 dB
    val agcMaxOutput: Float = -1.9f, // -6 to 0 dB

    // ViPER-DDC (Digital Driver Correction)
    val ddcEnabled: Boolean = false,
    val ddcDevice: String = "Sennheiser HD 650",

    // ViPER Clarity / Spectrum Extension
    val clarityEnabled: Boolean = true,
    val clarityMode: String = "Natural", // Natural, O-Zone, XHiFi
    val clarityGain: Float = 6.0f, // 0 to 14 dB

    // ViPER Dynamic Bass
    val bassEnabled: Boolean = true,
    val bassMode: String = "Natural Bass", // Natural Bass, Pure Bass, Sub-Woofer
    val bassFrequency: Int = 60, // 30, 40, 50, 60, 80, 100 Hz
    val bassGain: Float = 6.0f, // 0 to 14 dB

    // 10-Band Graphic Equalizer
    val eqEnabled: Boolean = true,
    val eqPresetName: String = "ViPER Signature",
    val eqBands: List<Float> = listOf(3.5f, 2.8f, 1.2f, 0.0f, -0.8f, 0.5f, 1.8f, 3.2f, 4.0f, 4.5f),

    // Convolver (IRS Simulation)
    val convolverEnabled: Boolean = false,
    val convolverIrs: String = "Tube Amp Warmth (Mullard 12AX7)",
    val convolverCrossfeed: Float = 40.0f, // 0% to 100%

    // Field Surround / VHS (ViPER Headphone Surround)
    val surroundEnabled: Boolean = false,
    val surroundLevel: Int = 2, // 1 to 5
    val midImage: Float = 50.0f, // 0 to 100%

    // AnalogX (Warm Tube Coloration)
    val analogXEnabled: Boolean = true,
    val analogXLevel: Int = 1, // 1: Level 1 (Warm), 2: Level 2 (Class-A), 3: Level 3 (Vintage)

    // Auditory System Protection (Cure Tech+)
    val cureTechEnabled: Boolean = false,
    val cureTechLevel: String = "Moderate" // Slight, Moderate, Extreme
)

object EqFrequencies {
    val labels = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    val centers = listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
}

object PresetProfiles {
    val BUILTIN_PRESETS = mapOf(
        "ViPER Signature" to listOf(3.5f, 2.8f, 1.2f, 0.0f, -0.8f, 0.5f, 1.8f, 3.2f, 4.0f, 4.5f),
        "Flat (Studio Reference)" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        "Audiophile Acoustic" to listOf(2.0f, 1.5f, 0.5f, 0.0f, 0.8f, 1.5f, 2.2f, 2.8f, 3.2f, 3.5f),
        "Bass Extreme" to listOf(9.0f, 7.5f, 5.0f, 2.5f, 0.5f, -0.5f, 0.0f, 1.0f, 2.0f, 2.5f),
        "EDM / Dance Punch" to listOf(6.5f, 5.0f, 2.5f, 0.5f, -0.5f, 1.2f, 2.5f, 4.0f, 5.5f, 6.0f),
        "Rock / Metal Arena" to listOf(4.5f, 3.0f, -1.0f, -2.0f, -0.5f, 2.0f, 4.0f, 4.5f, 3.5f, 4.0f),
        "Pop Clarity" to listOf(1.5f, 2.0f, 0.8f, 0.2f, 1.0f, 2.0f, 2.8f, 3.5f, 3.0f, 2.5f),
        "Jazz & Warm Vocals" to listOf(3.0f, 2.2f, 1.0f, 1.5f, 2.5f, 2.2f, 1.5f, 1.0f, 2.0f, 2.5f),
        "Treble Air & Sparkle" to listOf(-1.0f, -0.5f, 0.0f, 0.5f, 1.0f, 2.0f, 4.0f, 6.0f, 7.5f, 8.0f),
        "Classical Orchestra" to listOf(4.0f, 3.0f, 1.5f, 0.5f, -0.5f, 0.5f, 1.5f, 2.5f, 3.5f, 4.0f)
    )

    val DDC_DEVICES = listOf(
        "Sennheiser HD 650",
        "Audio-Technica ATH-M50x",
        "Sony WH-1000XM4 / XM5",
        "Apple AirPods Pro 2",
        "Apple AirPods Max",
        "Beyerdynamic DT 990 Pro",
        "Moondrop Aria / Kato",
        "Samsung Galaxy Buds 2 Pro",
        "Shure SE215 Sound Isolating",
        "HiFiMAN Sundara Planar",
        "Bose QuietComfort Ultra",
        "Sony IER-M9 Audiophile Monitor"
    )

    val CONVOLVER_IRS_LIST = listOf(
        "Tube Amp Warmth (Mullard 12AX7)",
        "Dolby Atmos Cinema Soundstage",
        "Lexicon 480L Studio Reverb",
        "Studer A800 Analog Tape 30ips",
        "Gold Vacuum Tube Harmonic",
        "Sony D-E01 Vintage MegaBass",
        "Vienna Symphony Concert Hall",
        "Abbey Road Studio Acoustics"
    )
}

data class DriverStatus(
    val version: String = "v2.7.2.1 NEON Enhanced",
    val neonEnabled: Boolean = true,
    val enabled: Boolean = true,
    val status: String = "Normal (Operational)",
    val audioFormat: String = "96,000 Hz, 32-bit Float",
    val processing: Boolean = true,
    val samplingRate: String = "96,000 Hz",
    val latency: String = "11.6 ms (Ultra Low Latency)"
)
