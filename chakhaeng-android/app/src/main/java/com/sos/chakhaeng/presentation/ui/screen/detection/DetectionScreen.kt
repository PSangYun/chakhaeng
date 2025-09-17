package com.sos.chakhaeng.presentation.ui.screen.detection

import android.Manifest
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.view.Surface
import android.view.TextureView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sos.chakhaeng.core.utils.camera.Camera2Pipeline
import com.sos.chakhaeng.core.utils.camera.RollingEncoder
import com.sos.chakhaeng.presentation.main.AppEntryViewModel
import com.sos.chakhaeng.presentation.model.ViolationDetectionUiModel
import com.sos.chakhaeng.presentation.theme.onPrimaryLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.components.detection.CameraErrorScreen
import com.sos.chakhaeng.presentation.ui.components.detection.CameraInactiveOverlay
import com.sos.chakhaeng.presentation.ui.components.detection.CameraLoadingScreen
import com.sos.chakhaeng.presentation.ui.components.detection.CameraPermissionRequest
import com.sos.chakhaeng.presentation.ui.components.detection.CameraPreviewSection
import com.sos.chakhaeng.presentation.ui.components.detection.ViolationDetectionSection

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = hiltViewModel(),
    onCreateNewViolation: () -> Unit,
    // id 생기면 대체
//    onViolationClick: (Long) -> Unit,
    onViolationClick: (ViolationDetectionUiModel) -> Unit,
    paddingValues: PaddingValues,
    appEntryViewModel: AppEntryViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }
    BackHandler(enabled = uiState.isFullscreen) {
        viewModel.toggleFullscreen()
    }

    // 1. 프리뷰용 TextureView -> Surface를 받아 Camera2에 전달
    var previewSurface by remember { mutableStateOf<Surface?>(null) }
    val textureView = remember {
        TextureView(context).apply {
            surfaceTextureListener = object: TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(tex: SurfaceTexture, w: Int, h: Int) {
                    previewSurface = Surface(tex)
                }

                override fun onSurfaceTextureSizeChanged(tex: SurfaceTexture, w: Int, h: Int) {

                }

                override fun onSurfaceTextureDestroyed(tex: SurfaceTexture): Boolean {
                    previewSurface = null; return true
                }

                override fun onSurfaceTextureUpdated(tex: SurfaceTexture) {

                }
            }
        }
    }

    // 2. RollingEncoder 준비
    val rolling = remember {
        RollingEncoder(context, width = 1280, height = 720, fps = 3, iFrameSec = 2)
    }

    // 3. Camera2 파이프라인 바인딩
    var pipeline by remember { mutableStateOf<Camera2Pipeline?>(null) }
    var starting by remember { mutableStateOf(false) }
    LaunchedEffect(previewSurface, uiState.cameraPermissionGranted) {
        val surface = previewSurface ?: return@LaunchedEffect
        if (!cameraPermission.status.isGranted) return@LaunchedEffect
        if (pipeline != null || starting) return@LaunchedEffect

        // encoder 시작 -> inputSurface 준비
        starting = true
        rolling.start()

        // 후면 카메라 선택
        val cameraId = chooseBackCamera(context)

        // 탐지용 ImageReader -> 여기에 AI 연결
        val analysisReader = ImageReader.newInstance(
            1280, 720, ImageFormat.YUV_420_888, 2
        ).apply {
            setOnImageAvailableListener({ r ->
                r.acquireLatestImage()?.use { image ->
                    // TODO: 온디바이스 AI 분석 연결 (감지되면 아래처럼 실행)
                }
            }, null)
        }

        val p = Camera2Pipeline(
            context = context,
            cameraId = cameraId,
            previewSurface = surface,
            analysisReader = analysisReader,
            encoderSurface = rolling.inputSurface,
            targetFps = 30
        )
        pipeline = p
        try {
            p.start()
        } catch (e: Exception) {
            viewModel.clearError()
        } finally {
            starting = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pipeline?.stop()
            pipeline = null
            rolling.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (uiState.isFullscreen) 1f else 0.3f)
            ) {
                when {
                    !cameraPermission.status.isGranted -> {
                        CameraPermissionRequest(
                            onRequestPermission = { cameraPermission.launchPermissionRequest() }
                        )
//                        CameraInactiveOverlay()
                    }

                    uiState.isLoading -> CameraLoadingScreen()
                        uiState.isDetectionActive && uiState.isCameraReady -> {
//                            CameraPreviewSection(
//                                isDetectionActive = uiState.isDetectionActive,
//                                isFullscreen = uiState.isFullscreen,
//                                onToggleFullscreen = {
//                                    viewModel.toggleFullscreen()
//                                },
//                                controller = appEntryViewModel.controller,
//                                textureView = textureView
//                            )
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { textureView }
                            )
                        }

                    !uiState.isDetectionActive -> CameraInactiveOverlay()
                    uiState.error != null -> {
                        CameraErrorScreen(
                            error = uiState.error!!,
                            onRetry = viewModel::clearError
                        )
                    }
                }
            }

            // 하단 위반 목록
            if (!uiState.isFullscreen) {
                ViolationDetectionSection(
                    selectedFilter = uiState.selectedViolationFilter,
                    violations = uiState.filteredViolations,
                    onFilterSelected = viewModel::onViolationFilterSelected,
                    onViolationClick = onViolationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }

//        FloatingActionButton(
//            onClick = {
//                LocalLifecycleOwner.current.lifecycleScope.launchWhenStarted {
//                    val uri = rolling.captureClip(preSec = 10, postSec = 10)
//                    viewModel.uploadClip(uri)
//                }
//            },
//            text = { Text("즉시 저장") },
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .padding(16.dp)
//        )

        FloatingActionButton(
            onClick = onCreateNewViolation,
            containerColor = primaryLight,
            contentColor = onPrimaryLight,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "새로 만들기")
        }
    }

}

private fun chooseBackCamera(context: Context): String {
    val mgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    return mgr.cameraIdList.first { id ->
        val chars = mgr.getCameraCharacteristics(id)
        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
    }
}
