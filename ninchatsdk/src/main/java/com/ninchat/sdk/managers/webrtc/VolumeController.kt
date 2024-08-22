package com.ninchat.sdk.managers.webrtc

import android.content.Context
import android.media.AudioManager

class VolumeController(context: Context) {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var previousMediaVolume: Int? = null
    private var previousCallVolume: Int? = null


    private fun setVolumeInternal(streamType: Int, volumeLevel: Int) {
        val maxVolume = getMaxVolume(streamType)
        val volume = volumeLevel.coerceIn(0, maxVolume)
        audioManager.setStreamVolume(streamType, volume, 0)
    }

    private fun getCurrentVolume(streamType: Int): Int {
        return audioManager.getStreamVolume(streamType)
    }

    private fun getMaxVolume(streamType: Int): Int {
        return audioManager.getStreamMaxVolume(streamType)
    }

    private fun setVolumeToMax(streamType: Int) {
        val maxVolume = getMaxVolume(streamType)
        setVolumeInternal(streamType, maxVolume)
    }

    fun setVolume() {
        previousMediaVolume = getMediaVolume()
        previousCallVolume = getCallVolume()

        setVolumeToMax(AudioManager.STREAM_MUSIC)
        setVolumeToMax(AudioManager.STREAM_VOICE_CALL)
    }

    fun resetVolume() {
        previousMediaVolume?.let { setMediaVolume(it) }
        previousCallVolume?.let { setCallVolume(it) }
    }

    private fun setMediaVolume(volumeLevel: Int) = setVolumeInternal(AudioManager.STREAM_MUSIC, volumeLevel)
    private fun setCallVolume(volumeLevel: Int) = setVolumeInternal(AudioManager.STREAM_VOICE_CALL, volumeLevel)

    private fun getMediaVolume() = getCurrentVolume(AudioManager.STREAM_MUSIC)
    private fun getCallVolume() = getCurrentVolume(AudioManager.STREAM_VOICE_CALL)
}