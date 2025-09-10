package com.sos.chakhaeng.presentation.ui.screen.streaming.component

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.sos.chakhaeng.core.utils.findActivity
import com.sos.chakhaeng.core.utils.setFullscreen
import com.sos.chakhaeng.presentation.ui.screen.streaming.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    url: String,
    mimeType: String? = MimeTypes.APPLICATION_M3U8,
    autoPlay: Boolean = true,
    useController: Boolean = false,
    initialMute: Boolean = false,
    rotateOnFullscreen: Boolean = true,
    onBackFromFullscreen: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(url, mimeType, autoPlay, initialMute) {
        playerViewModel.setSource(url, mimeType, autoPlay, initialMute)
        playerViewModel.showControls(autoHide = true)
    }

    BackHandler(enabled = uiState.isFullscreen) {
        playerViewModel.setFullscreen(false)
        activity?.setFullscreen(false)
        if (rotateOnFullscreen) activity?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onBackFromFullscreen?.invoke()
    }

    LaunchedEffect(uiState.isFullscreen) {
        activity?.setFullscreen(uiState.isFullscreen)
        if (rotateOnFullscreen) {
            activity?.requestedOrientation =
                if (uiState.isFullscreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.setFullscreen(false)
            if (rotateOnFullscreen) activity?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val density = LocalDensity.current
    val triggerPx = with(density) { 64.dp.toPx() }
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .then(
                if (uiState.isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
                    .aspectRatio(16 / 9f)
            )
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { playerViewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        val width = this.size.width
                        if (offset.x < width / 2f) playerViewModel.seekBack() else playerViewModel.seekForward()
                        playerViewModel.showControls()
                    })
            }
            .pointerInput(isPortrait, uiState.isFullscreen) {
                if (isPortrait && !uiState.isFullscreen) {
                    detectDragGestures(onDrag = { _, dragAmount ->
                        dragAccum += dragAmount.y
                        if (dragAccum > triggerPx) {
                            dragAccum = 0f
                            playerViewModel.setFullscreen(true)
                        }
                    }, onDragEnd = { dragAccum = 0f }, onDragCancel = { dragAccum = 0f })
                }
            }) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
            PlayerView(ctx).apply {
                this.useController = useController
                resizeMode = if (uiState.isFullscreen) AspectRatioFrameLayout.RESIZE_MODE_FIT
                else AspectRatioFrameLayout.RESIZE_MODE_FIT
                this.player = playerViewModel.player
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                keepScreenOn = true
            }
        }, update = { pv ->
            pv.player = playerViewModel.player
            pv.resizeMode = if (uiState.isFullscreen) AspectRatioFrameLayout.RESIZE_MODE_FIT
            else AspectRatioFrameLayout.RESIZE_MODE_FIT
        })
        if (uiState.showPoster && uiState.posterModel != null) {
            AsyncImage(
                model = uiState.posterModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else if (uiState.showPoster) {
            // posterModel이 없는데 포스터가 필요한 경우(파일 기반): Coil VideoFrameDecoder로 프레임 썸네일 시도
            // (m3u8 HLS는 실패할 수 있으므로 서버 포스터 권장)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(playerViewModel.player.currentMediaItem?.localConfiguration?.uri)
                    .videoFrameMillis(500) // 0.5s 지점 프레임
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
        AnimatedVisibility(
            visible = uiState.showSpinner,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) { CircularProgressIndicator() }

        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "재생 오류가 발생했어요.\n${uiState.error?.localizedMessage ?: ""}",
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                Button(onClick = {
                    playerViewModel.clearError()
                    playerViewModel.player.prepare()
                    playerViewModel.player.playWhenReady = true
                }) { Text("다시 시도") }
            }
        }

        AnimatedVisibility(
            visible = uiState.controlsVisible && uiState.error == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            ControlOverlay(
                isPlaying = uiState.isPlaying,
                durationMs = uiState.durationMs,
                positionMs = uiState.positionMs,
                onPlayPause = playerViewModel::playPause,
                onSeekBack = playerViewModel::seekBack,
                onSeekForward = playerViewModel::seekForward,
                onSeekTo = playerViewModel::seekTo,
                onScrubStart = { playerViewModel.setUserScrubbing(true) },
                onScrubEnd = { playerViewModel.setUserScrubbing(false) },
                onToggleFullscreen = {
                    val to = !uiState.isFullscreen
                    playerViewModel.setFullscreen(to)
                })
        }
    }
}