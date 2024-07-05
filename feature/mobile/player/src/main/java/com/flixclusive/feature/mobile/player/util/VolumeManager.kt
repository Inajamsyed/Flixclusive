package com.flixclusive.feature.mobile.player.util

import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

internal val LocalVolumeManager = compositionLocalOf<VolumeManager> {
    error("VolumeManager not provided")
}

@Composable
internal fun rememberVolumeManager()
    = rememberUpdatedState(LocalVolumeManager.current).value

class VolumeManager(private val audioManager: AudioManager) {
    var loudnessEnhancer: LoudnessEnhancer? = null
        set(value) {
            if (currentVolume > maxStreamVolume) {
                try {
                    value?.enabled = true
                    value?.setTargetGain(currentLoudnessGain.toInt())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            field = value
        }

    private val currentStreamVolume
        get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    private val maxStreamVolume
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    var currentVolume by mutableFloatStateOf(currentStreamVolume.toFloat())
        private set
    val currentVolumePercentage by derivedStateOf {
        (currentVolume / maxStreamVolume.toFloat())
            .coerceIn(0F, 1F)
    }
    val maxVolume
        get() = maxStreamVolume
            .times(loudnessEnhancer?.let { 2 } ?: 1)
            .toFloat()

    private val currentLoudnessGain
        get() = (currentVolume - maxStreamVolume) * (MAX_VOLUME_BOOST / maxStreamVolume)

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0F, maxVolume)

        if (currentVolume <= maxStreamVolume) {
            loudnessEnhancer?.enabled = false
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume.toInt(),
                0
            )
        } else {
            try {
                loudnessEnhancer?.enabled = true
                loudnessEnhancer?.setTargetGain(currentLoudnessGain.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun increaseVolume() {
        setVolume(currentVolume + 1)
    }

    fun decreaseVolume() {
        setVolume(currentVolume - 1)
    }

    companion object {
        const val MAX_VOLUME_BOOST = 2000
    }
}