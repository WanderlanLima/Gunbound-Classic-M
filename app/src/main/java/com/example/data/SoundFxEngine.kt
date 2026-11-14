package com.example.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundFxEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    var enabled: Boolean = true

    fun playClick() {
        if (!enabled) return
        playTone(freqStart = 760f, freqEnd = 520f, durationMs = 45, noiseMix = 0.05f, volume = 0.25f)
    }

    fun playLaunch(heavy: Boolean = false) {
        if (!enabled) return
        if (heavy) {
            playTone(freqStart = 280f, freqEnd = 90f, durationMs = 210, noiseMix = 0.45f, volume = 0.45f)
        } else {
            playTone(freqStart = 420f, freqEnd = 160f, durationMs = 150, noiseMix = 0.25f, volume = 0.35f)
        }
    }

    fun playExplosion(large: Boolean = false) {
        if (!enabled) return
        val dur = if (large) 380 else 240
        playTone(freqStart = 140f, freqEnd = 36f, durationMs = dur, noiseMix = 0.85f, volume = 0.55f)
    }

    fun playVictory() {
        if (!enabled) return
        scope.launch {
            playRawNotes(listOf(523.25f to 110, 659.25f to 110, 783.99f to 110, 1046.5f to 260))
        }
    }

    fun playDefeat() {
        if (!enabled) return
        scope.launch {
            playRawNotes(listOf(392.0f to 140, 349.23f to 140, 311.13f to 280))
        }
    }

    private fun playTone(
        freqStart: Float,
        freqEnd: Float,
        durationMs: Int,
        noiseMix: Float,
        volume: Float
    ) {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val count = (sampleRate * durationMs) / 1000
                val samples = ShortArray(count)
                var phase = 0.0
                for (i in 0 until count) {
                    val t = i.toFloat() / count
                    val freq = freqStart + (freqEnd - freqStart) * t
                    phase += 2.0 * PI * freq / sampleRate
                    val env = exp(-4.2 * t).toFloat()
                    val tone = sin(phase).toFloat() * (1f - noiseMix)
                    val noise = (Random.nextFloat() * 2f - 1f) * noiseMix
                    val v = ((tone + noise) * env * volume * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    samples[i] = v.toShort()
                }
                writePcm(sampleRate, samples)
            }
        }
    }

    private fun playRawNotes(notes: List<Pair<Float, Int>>) {
        runCatching {
            val sampleRate = 22050
            val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
            val samples = ShortArray(totalSamples)
            var idx = 0
            for ((freq, durMs) in notes) {
                val count = (sampleRate * durMs) / 1000
                var phase = 0.0
                for (i in 0 until count) {
                    val t = i.toFloat() / count
                    phase += 2.0 * PI * freq / sampleRate
                    val env = exp(-2.5 * t).toFloat()
                    val v = (sin(phase).toFloat() * env * 0.35f * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    samples[idx++] = v.toShort()
                }
            }
            writePcm(sampleRate, samples)
        }
    }

    private fun writePcm(sampleRate: Int, samples: ShortArray) {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(samples.size * 2)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        track.play()
        Thread.sleep((samples.size * 1000L / sampleRate) + 40L)
        track.stop()
        track.release()
    }
}
