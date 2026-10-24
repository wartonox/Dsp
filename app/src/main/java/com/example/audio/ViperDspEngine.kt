package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.model.EqFrequencies
import com.example.model.ViperProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

class ViperDspEngine(private val scope: CoroutineScope) {

    private val sampleRate = 44100
    private val bufferSize = 2048
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex = _currentTrackIndex.asStateFlow()

    val trackNames = listOf(
        "Neon Cyberpunk Beat (Bass & EQ Test)",
        "Smooth Jazz & Sax (Warmth & AnalogX)",
        "Acoustic Guitar & Piano (Clarity & IRS)",
        "Pink Noise & 440Hz (Reference Calibrator)"
    )

    private val _spectrumBins = MutableStateFlow(FloatArray(32))
    val spectrumBins = _spectrumBins.asStateFlow()

    @Volatile
    private var currentProfile: ViperProfile = ViperProfile()

    init {
        initAudioTrack()
    }

    fun updateProfile(profile: ViperProfile) {
        currentProfile = profile
    }

    private fun initAudioTrack() {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val trackBufferSize = max(minBufSize, bufferSize * 4)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(trackBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e("ViperDspEngine", "Error initializing AudioTrack", e)
        }
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (_isPlaying.value) return
        if (audioTrack == null || audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            initAudioTrack()
        }

        try {
            audioTrack?.play()
            _isPlaying.value = true

            playbackJob?.cancel()
            playbackJob = scope.launch(Dispatchers.Default) {
                renderAudioLoop()
            }
        } catch (e: Exception) {
            Log.e("ViperDspEngine", "Play error", e)
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            Log.e("ViperDspEngine", "Pause error", e)
        }
    }

    fun selectTrack(index: Int) {
        _currentTrackIndex.value = index.coerceIn(0, trackNames.size - 1)
    }

    private fun renderAudioLoop() {
        val shortBuffer = ShortArray(bufferSize * 2) // Interleaved stereo
        val floatL = FloatArray(bufferSize)
        val floatR = FloatArray(bufferSize)
        var phaseMain = 0.0
        var phaseBass = 0.0
        var phaseHiHat = 0.0
        var stepCounter = 0

        // Delay lines for Convolver & Surround
        val delayBufferL = FloatArray(sampleRate / 4) // max 250ms
        val delayBufferR = FloatArray(sampleRate / 4)
        var delayIdx = 0

        while (scope.isActive && _isPlaying.value) {
            val trackIdx = _currentTrackIndex.value
            val profile = currentProfile

            // 1. Synthesize Track Samples
            for (i in 0 until bufferSize) {
                stepCounter++
                val timeSec = stepCounter.toDouble() / sampleRate
                var rawL = 0.0
                var rawR = 0.0

                when (trackIdx) {
                    0 -> {
                        // Neon Cyberpunk Beat: Sub Kick, Bassline, Snare, Synth Arp
                        val beatTime = (timeSec * 2.2) % 4.0 // 132 BPM
                        // Kick on beat 0 and 2
                        val kickPhase = (beatTime % 1.0)
                        val kick = if (kickPhase < 0.25) {
                            val f = 140.0 * exp(-kickPhase * 18.0) + 42.0
                            sin(2.0 * PI * f * kickPhase) * exp(-kickPhase * 8.0) * 0.8
                        } else 0.0

                        // Snare on beat 1 and 3
                        val snarePhase = ((beatTime + 0.5) % 1.0)
                        val snare = if (snarePhase < 0.2) {
                            val noise = (Math.random() * 2.0 - 1.0) * exp(-snarePhase * 14.0)
                            val tone = sin(2.0 * PI * 220.0 * snarePhase) * exp(-snarePhase * 18.0)
                            (noise * 0.4 + tone * 0.3)
                        } else 0.0

                        // Bassline (Saw synth)
                        val noteFreq = when (((beatTime * 2).toInt()) % 8) {
                            0, 1 -> 55.0 // A1
                            2 -> 65.4 // C2
                            3, 4 -> 73.4 // D2
                            5 -> 82.4 // E2
                            6 -> 49.0 // G1
                            else -> 55.0
                        }
                        phaseBass = (phaseBass + 2.0 * PI * noteFreq / sampleRate) % (2.0 * PI)
                        val bass = (sin(phaseBass) + 0.5 * sin(2.0 * phaseBass) + 0.25 * sin(3.0 * phaseBass)) * 0.35

                        // Arpeggiated high synth
                        val arpNotes = doubleArrayOf(440.0, 523.25, 659.25, 880.0, 1046.5)
                        val arpIdx = ((beatTime * 8).toInt()) % arpNotes.size
                        val arpFreq = arpNotes[arpIdx]
                        phaseMain = (phaseMain + 2.0 * PI * arpFreq / sampleRate) % (2.0 * PI)
                        val arp = sin(phaseMain) * 0.15

                        // Hi-hat
                        val hatPhase = (beatTime * 4.0) % 1.0
                        val hat = if (hatPhase < 0.06) (Math.random() * 2.0 - 1.0) * 0.12 else 0.0

                        rawL = kick + snare * 0.8 + bass * 0.9 + arp * 0.7 + hat
                        rawR = kick + snare * 0.9 + bass * 0.9 + arp * 1.1 + hat * 0.9
                    }
                    1 -> {
                        // Smooth Jazz: Double Bass, Rhodes Chord, Saxophone tone
                        val chordTime = timeSec * 1.2
                        val chordRoot = when (((chordTime / 2).toInt()) % 4) {
                            0 -> 174.61 // F3
                            1 -> 196.00 // G3
                            2 -> 220.00 // A3
                            else -> 146.83 // D3
                        }
                        val bassF = chordRoot / 2.0
                        phaseBass = (phaseBass + 2.0 * PI * bassF / sampleRate) % (2.0 * PI)
                        val bassTone = (sin(phaseBass) + 0.3 * sin(2.0 * phaseBass)) * 0.45

                        // Rhodes soft piano chords
                        val c1 = sin(2.0 * PI * chordRoot * timeSec) * 0.12
                        val c2 = sin(2.0 * PI * (chordRoot * 1.25) * timeSec) * 0.10
                        val c3 = sin(2.0 * PI * (chordRoot * 1.5) * timeSec) * 0.10

                        // Sax lead with vibrato
                        val vib = 1.0 + 0.015 * sin(2.0 * PI * 5.0 * timeSec)
                        val leadFreq = chordRoot * 2.0 * vib
                        phaseMain = (phaseMain + 2.0 * PI * leadFreq / sampleRate) % (2.0 * PI)
                        val lead = (sin(phaseMain) + 0.4 * sin(2.0 * phaseMain) + 0.2 * sin(3.0 * phaseMain)) * 0.22

                        rawL = bassTone + c1 + c2 + lead * 0.85
                        rawR = bassTone + c2 + c3 + lead * 0.95
                    }
                    2 -> {
                        // Acoustic Guitar & Piano
                        val beat = (timeSec * 2.0) % 8.0
                        val freqRoot = 261.63 // C4
                        val f1 = freqRoot * (if (beat < 4.0) 1.0 else 1.2)
                        phaseMain = (phaseMain + 2.0 * PI * f1 / sampleRate) % (2.0 * PI)
                        val acoustic = sin(phaseMain) * exp(-(beat % 1.0) * 3.0) * 0.4

                        val pianoF = freqRoot * 2.0
                        val piano = sin(2.0 * PI * pianoF * timeSec) * exp(-(beat % 2.0) * 2.0) * 0.25

                        rawL = acoustic * 0.9 + piano * 0.7
                        rawR = acoustic * 0.7 + piano * 0.9
                    }
                    else -> {
                        // Reference Tone: 440 Hz + Subtle Pink Noise
                        val testTone = sin(2.0 * PI * 440.0 * timeSec) * 0.35
                        val noise = (Math.random() * 2.0 - 1.0) * 0.08
                        rawL = testTone + noise
                        rawR = testTone + noise
                    }
                }

                floatL[i] = rawL.toFloat()
                floatR[i] = rawR.toFloat()
            }

            // 2. Apply ViPER DSP Processing Pipeline
            if (profile.masterEnabled) {
                // A. ViPER Dynamic Bass
                if (profile.bassEnabled && profile.bassGain > 0.01f) {
                    val bassMult = 1.0f + (profile.bassGain / 4.0f)
                    val modeHarmonics = when (profile.bassMode) {
                        "Pure Bass" -> 1.3f
                        "Sub-Woofer" -> 1.8f
                        else -> 1.0f // Natural Bass
                    }
                    for (i in 0 until bufferSize) {
                        val subL = floatL[i] * bassMult * modeHarmonics
                        val subR = floatR[i] * bassMult * modeHarmonics
                        // Low end punch saturation
                        floatL[i] = (floatL[i] + tanh(subL * 0.6f) * 0.5f)
                        floatR[i] = (floatR[i] + tanh(subR * 0.6f) * 0.5f)
                    }
                }

                // B. 10-Band Graphic Equalizer
                if (profile.eqEnabled) {
                    val bands = profile.eqBands
                    // Simplified fast 3-band / 10-band response weighting for live demo
                    val lowGain = dbToLin((bands.getOrElse(0) { 0f } + bands.getOrElse(1) { 0f } + bands.getOrElse(2) { 0f }) / 3f)
                    val midGain = dbToLin((bands.getOrElse(3) { 0f } + bands.getOrElse(4) { 0f } + bands.getOrElse(5) { 0f } + bands.getOrElse(6) { 0f }) / 4f)
                    val highGain = dbToLin((bands.getOrElse(7) { 0f } + bands.getOrElse(8) { 0f } + bands.getOrElse(9) { 0f }) / 3f)

                    val eqBlend = (lowGain * 0.33f + midGain * 0.34f + highGain * 0.33f)
                    for (i in 0 until bufferSize) {
                        floatL[i] *= eqBlend
                        floatR[i] *= eqBlend
                    }
                }

                // C. ViPER Clarity (Treble Exciter)
                if (profile.clarityEnabled && profile.clarityGain > 0.01f) {
                    val clarityFactor = profile.clarityGain / 10.0f
                    val modeColor = when (profile.clarityMode) {
                        "XHiFi" -> 1.4f
                        "O-Zone" -> 1.2f
                        else -> 0.9f // Natural
                    }
                    for (i in 0 until bufferSize) {
                        val diffL = if (i > 0) floatL[i] - floatL[i - 1] else 0f
                        val diffR = if (i > 0) floatR[i] - floatR[i - 1] else 0f
                        floatL[i] += diffL * clarityFactor * modeColor
                        floatR[i] += diffR * clarityFactor * modeColor
                    }
                }

                // D. AnalogX (Vacuum Tube Saturation)
                if (profile.analogXEnabled) {
                    val drive = 1.0f + profile.analogXLevel * 0.4f
                    for (i in 0 until bufferSize) {
                        floatL[i] = tanh(floatL[i] * drive) / drive * 1.15f
                        floatR[i] = tanh(floatR[i] * drive) / drive * 1.15f
                    }
                }

                // E. Field Surround / VHS (Mid-Side Stereo Spreader)
                if (profile.surroundEnabled) {
                    val width = 1.0f + (profile.surroundLevel * 0.25f)
                    for (i in 0 until bufferSize) {
                        val mid = (floatL[i] + floatR[i]) * 0.5f
                        val side = (floatL[i] - floatR[i]) * 0.5f * width
                        floatL[i] = mid + side
                        floatR[i] = mid - side
                    }
                }

                // F. Convolver IRS Simulation (Crossfeed + Acoustic Space Delay)
                if (profile.convolverEnabled) {
                    val mix = (profile.convolverCrossfeed / 100f).coerceIn(0f, 0.8f)
                    val delaySamples = 1200 // approx 27ms acoustic reflection
                    for (i in 0 until bufferSize) {
                        val dIdx = (delayIdx + i) % delayBufferL.size
                        val delayedL = delayBufferL[dIdx]
                        val delayedR = delayBufferR[dIdx]

                        delayBufferL[dIdx] = floatL[i]
                        delayBufferR[dIdx] = floatR[i]

                        floatL[i] = floatL[i] * (1f - mix * 0.4f) + delayedR * mix * 0.5f
                        floatR[i] = floatR[i] * (1f - mix * 0.4f) + delayedL * mix * 0.5f
                    }
                    delayIdx = (delayIdx + bufferSize) % delayBufferL.size
                }

                // G. Playback AGC & Master Gain & Pan
                val masterVol = dbToLin(profile.masterGain)
                val panL = (1f - profile.outputPan).coerceIn(0f, 2f) / 1f
                val panR = (1f + profile.outputPan).coerceIn(0f, 2f) / 1f
                val limiter = dbToLin(profile.limiterThreshold).coerceIn(0.1f, 1.0f)

                for (i in 0 until bufferSize) {
                    var sL = floatL[i] * masterVol * panL
                    var sR = floatR[i] * masterVol * panR

                    // Hard/Soft Limiter to prevent clipping
                    if (abs(sL) > limiter) sL = sign(sL) * limiter
                    if (abs(sR) > limiter) sR = sign(sR) * limiter

                    floatL[i] = sL
                    floatR[i] = sR
                }
            }

            // 3. Convert float to 16-bit PCM and calculate live spectrum
            val spectrumChunk = FloatArray(32)
            val step = bufferSize / 32
            for (bin in 0 until 32) {
                var sum = 0f
                val start = bin * step
                val end = min(start + step, bufferSize)
                for (j in start until end) {
                    sum += abs(floatL[j]) + abs(floatR[j])
                }
                val avg = sum / (end - start + 1)
                // Shape spectrum visually: lower frequencies have higher energy, normalize gracefully
                val eqBoost = if (profile.masterEnabled && profile.eqEnabled) {
                    val eqIndex = (bin * 10 / 32).coerceIn(0, 9)
                    (profile.eqBands.getOrElse(eqIndex) { 0f } + 12f) / 24f
                } else 0.5f
                spectrumChunk[bin] = ((avg * 1.6f).pow(0.8f) * (0.6f + eqBoost * 0.8f)).coerceIn(0.05f, 1.0f)
            }
            _spectrumBins.value = spectrumChunk

            // Fill audio buffer
            var bufIdx = 0
            for (i in 0 until bufferSize) {
                val sampleL = (floatL[i].coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
                val sampleR = (floatR[i].coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
                shortBuffer[bufIdx++] = sampleL
                shortBuffer[bufIdx++] = sampleR
            }

            audioTrack?.write(shortBuffer, 0, shortBuffer.size)
        }
    }

    private fun dbToLin(dB: Float): Float {
        return 10.0f.pow(dB / 20.0f)
    }

    fun release() {
        pause()
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e("ViperDspEngine", "Release error", e)
        }
    }
}
