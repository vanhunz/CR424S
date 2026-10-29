package com.example.nguyencongnhathuy_29214654999_cr424s.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import kotlin.math.PI
import kotlin.math.sin

object GameAudio {
    @Volatile
    private var musicEnabled = false
    private var track: AudioTrack? = null
    private var musicThread: Thread? = null

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (enabled) startMusic() else stopMusic()
    }

    fun playMove() = playTone(ToneGenerator.TONE_PROP_BEEP, 45)
    fun playWin() = playTone(ToneGenerator.TONE_PROP_ACK, 180)
    fun playError() = playTone(ToneGenerator.TONE_PROP_NACK, 120)

    private fun playTone(tone: Int, duration: Int) {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70).apply {
            startTone(tone, duration)
            release()
        }
    }

    private fun startMusic() {
        if (musicThread?.isAlive == true) return
        val sampleRate = 8_000
        val bufferSize = sampleRate / 2
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        val notes = doubleArrayOf(261.63, 329.63, 392.0, 329.63, 293.66, 349.23, 440.0, 349.23)
        musicThread = Thread {
            val data = ShortArray(bufferSize)
            track?.play()
            var note = 0
            while (musicEnabled) {
                val frequency = notes[note++ % notes.size]
                for (i in data.indices) {
                    val envelope = 0.12 + 0.08 * sin(i * PI / data.size)
                    data[i] = (sin(2 * PI * frequency * i / sampleRate) * Short.MAX_VALUE * envelope).toInt().toShort()
                }
                track?.write(data, 0, data.size)
            }
        }.also { it.start() }
    }

    private fun stopMusic() {
        musicThread = null
        track?.let {
            runCatching { it.stop() }
            it.release()
        }
        track = null
    }
}
