package com.sos.chakhaeng.presentation.ui.screen.streaming

import androidx.media3.common.PlaybackException

data class PlayerUiState(
    val controlsVisible: Boolean = true,
    val isFullscreen: Boolean = false,
    val isMuted: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: PlaybackException? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val userScrubbing: Boolean = false
)