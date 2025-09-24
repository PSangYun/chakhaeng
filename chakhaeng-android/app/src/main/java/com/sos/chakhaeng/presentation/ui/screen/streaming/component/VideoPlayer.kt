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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.sos.chakhaeng.core.utils.findActivity
import com.sos.chakhaeng.core.utils.setFullscreen
import com.sos.chakhaeng.presentation.ui.screen.streaming.PlayerUiState
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

    // 소스 세팅
    LaunchedEffect(url, mimeType, autoPlay, initialMute) {
        playerViewModel.setSource(url, mimeType, autoPlay, initialMute)
        playerViewModel.showControls(autoHide = true)
    }

    // === 일반(임베디드) 플레이어: 16:9 박스 안에만 표시 ===
    if (!uiState.isFullscreen) {
        EmbeddedPlayerBox(
            url = url,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .background(Color.Black),
            playerViewModel = playerViewModel,
            useController = useController,
            onRequestFullscreen = { playerViewModel.setFullscreen(true) },
            enableDragToFullscreen = isPortrait, // 세로에서만 위로 드래그로 전체화면
        )
    }

    // === 전체화면 모드: Dialog로 분리 표시 ===
    if (uiState.isFullscreen) {
        // 진입 시 시스템바/회전 처리
        LaunchedEffect(Unit) {
            activity?.setFullscreen(true)
            if (rotateOnFullscreen) {
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
        // 빠져나갈 때 복원
        DisposableEffect(Unit) {
            onDispose {
                activity?.setFullscreen(false)
                if (rotateOnFullscreen) {
                    activity?.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                // 백스와이프(제스처)로 닫히는 걸 막고 싶으면 빈 람다로 두고 BackHandler로만 닫게 하자.
            },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,      // 가로 폭 제한 제거
                decorFitsSystemWindows = false,       // 시스템바 아래까지 차지
                dismissOnBackPress = false,           // BackHandler로 제어
                dismissOnClickOutside = false
            )
        ) {
            // Back 동작: 전체화면 해제
            BackHandler(true) {
                playerViewModel.setFullscreen(false)
                onBackFromFullscreen?.invoke()
            }

            // 전체화면 레이어
            FullscreenPlayerLayer(
                url = url,
                playerViewModel = playerViewModel,
                useController = useController,
                onExit = {
                    playerViewModel.setFullscreen(false)
                    onBackFromFullscreen?.invoke()
                }
            )
        }
    }
}

/** 일반(임베디드) 16:9 컨테이너 안의 플레이어 + 제스처 */
@Composable
private fun EmbeddedPlayerBox(
    modifier: Modifier,
    playerViewModel: PlayerViewModel,
    useController: Boolean,
    onRequestFullscreen: () -> Unit,
    enableDragToFullscreen: Boolean,
    url: String
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val triggerPx = with(density) { 64.dp.toPx() }
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { playerViewModel.toggleControls() },
                    onDoubleTap = { offset ->
                        val width = this.size.width
                        if (offset.x < width / 2f) playerViewModel.seekBack()
                        else playerViewModel.seekForward()
                        playerViewModel.showControls()
                    }
                )
            }
            .then(
                if (enableDragToFullscreen)
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { _, dragAmount ->
                                dragAccum += dragAmount.y
                                if (dragAccum < -triggerPx) { // 위로 스와이프 시 전체화면
                                    dragAccum = 0f
                                    onRequestFullscreen()
                                }
                            },
                            onDragEnd = { dragAccum = 0f },
                            onDragCancel = { dragAccum = 0f }
                        )
                    }
                else Modifier
            )
    ) {
        PlayerAndroidView(
            playerViewModel = playerViewModel,
            useController = useController,
            fullscreen = false
        )

        PosterSpinnerAndError(
            modifier = Modifier.align(Alignment.Center),
            url = url,
            uiState = uiState,
            onRetry = {
                playerViewModel.clearError()
                playerViewModel.player.prepare()
                playerViewModel.player.playWhenReady = true
            }
        )

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
                onToggleFullscreen = { onRequestFullscreen() }
            )
        }
    }
}

/** 전체화면 레이어(Dialog 콘텐츠) */
@Composable
private fun FullscreenPlayerLayer(
    playerViewModel: PlayerViewModel,
    useController: Boolean,
    onExit: () -> Unit,
    url: String,
) {
    val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PlayerAndroidView(
            playerViewModel = playerViewModel,
            useController = useController,
            fullscreen = true
        )

        PosterSpinnerAndError(
            url = url,
            uiState = uiState,
            onRetry = {
                playerViewModel.clearError()
                playerViewModel.player.prepare()
                playerViewModel.player.playWhenReady = true
            },
            modifier = Modifier.align(Alignment.Center)
        )

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
                onToggleFullscreen = { onExit() } // 전체화면에서 누르면 닫기
            )
        }
    }
}

/** 안드로이드 PlayerView (공통) */
@OptIn(UnstableApi::class)
@Composable
private fun PlayerAndroidView(
    playerViewModel: PlayerViewModel,
    useController: Boolean,
    fullscreen: Boolean
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.useController = useController
                resizeMode = if (fullscreen)
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM  // 전체화면은 꽉 채우기
                else
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                this.player = playerViewModel.player
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                keepScreenOn = true
            }
        },
        update = { pv ->
            pv.player = playerViewModel.player
            pv.resizeMode = if (fullscreen)
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            else
                AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    )
}

/** 포스터/스피너/에러 공통 오버레이 */
@Composable
private fun PosterSpinnerAndError(
    url: String,
    uiState: PlayerUiState,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    if (uiState.showPoster && uiState.posterModel != null) {
        coil.compose.AsyncImage(
            model = uiState.posterModel,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else if (uiState.showPoster) {
        coil.compose.AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .videoFrameMillis(500)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    AnimatedVisibility(
        visible = uiState.showSpinner,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        CircularProgressIndicator()
    }

    AnimatedVisibility(
        visible = uiState.error != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "재생 오류가 발생했어요.\n${uiState.error?.localizedMessage ?: ""}",
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) { Text("다시 시도") }
        }
    }
}
