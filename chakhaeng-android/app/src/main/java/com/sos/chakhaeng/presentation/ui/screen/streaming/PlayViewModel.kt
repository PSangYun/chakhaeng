package com.sos.chakhaeng.presentation.ui.screen.streaming

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val app: Context,
    val player: ExoPlayer
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private var hideJob: Job? = null
    private var tickerJob: Job? = null

    init {
        // 기본 스케일링(비율 유지)
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
            override fun onIsLoadingChanged(isLoading: Boolean) {
                _uiState.update { it.copy(isBuffering = isLoading) }
            }
            override fun onPlayerError(error: PlaybackException) {
                _uiState.update { it.copy(error = error) }
            }
        })

        startTicker()
    }
    fun setSource(url: String, mimeType: String? = MimeTypes.APPLICATION_M3U8, autoPlay: Boolean, initialMute: Boolean) {
        if (player.mediaItemCount == 0 || player.currentMediaItem?.localConfiguration?.uri.toString() != url) {
            val item = MediaItem.Builder().setUri(url).setMimeType(mimeType).build()
            player.setMediaItem(item)
        }
        player.playWhenReady = autoPlay
        setMuted(initialMute)
        player.prepare()
    }

    fun playPause() { if (player.isPlaying) player.pause() else player.play() }
    fun seekBack() = player.seekBack()
    fun seekForward() = player.seekForward()
    fun seekTo(ms: Long) = player.seekTo(ms)

    fun setMuted(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
        _uiState.update { it.copy(isMuted = muted) }
    }

    fun setFullscreen(full: Boolean) = _uiState.update { it.copy(isFullscreen = full) }

    fun toggleControls(autoHide: Boolean = true) {
        val now = !_uiState.value.controlsVisible
        _uiState.update { it.copy(controlsVisible = now) }
        if (now && autoHide) scheduleAutoHide()
        if (!now) hideJob?.cancel()
    }
    fun showControls(autoHide: Boolean = true) {
        _uiState.update { it.copy(controlsVisible = true) }
        if (autoHide) scheduleAutoHide()
    }
    private fun scheduleAutoHide() {
        hideJob?.cancel()
        hideJob = viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(controlsVisible = false) }
        }
    }

    fun setUserScrubbing(scrub: Boolean) = _uiState.update { it.copy(userScrubbing = scrub) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val dur = player.duration.takeUnless { it == C.TIME_UNSET } ?: 0L
                _uiState.update {
                    it.copy(
                        positionMs = if (it.userScrubbing) it.positionMs else player.currentPosition.coerceAtLeast(0L),
                        durationMs = dur,
                        bufferedMs = player.bufferedPosition.coerceAtLeast(0L)
                    )
                }
                delay(250)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}