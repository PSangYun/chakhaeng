package com.sos.chakhaeng.presentation.ui.screen.detection

import android.Manifest
import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sos.chakhaeng.core.ai.LaneDetection
import com.sos.chakhaeng.presentation.theme.onPrimaryLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.components.detection.CameraErrorScreen
import com.sos.chakhaeng.presentation.ui.components.detection.CameraInactiveOverlay
import com.sos.chakhaeng.presentation.ui.components.detection.CameraLoadingScreen
import com.sos.chakhaeng.presentation.ui.components.detection.CameraPermissionRequest
import com.sos.chakhaeng.presentation.ui.components.detection.CameraPreviewSection
import com.sos.chakhaeng.presentation.ui.components.detection.DetectionOverlay
import com.sos.chakhaeng.presentation.ui.components.detection.TrackingOverlay
import com.sos.chakhaeng.presentation.ui.components.detection.ViolationDetectionSection
import com.sos.chakhaeng.recording.CameraRecordingService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var service by remember { mutableStateOf<CameraRecordingService?>(null) }
    val detections by remember(service) {
        service?.detectionsFlow() ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val laneDetection by remember(service) {
        service?.lanesFlow() ?: flowOf(LaneDetection())
    }.collectAsStateWithLifecycle(initialValue = LaneDetection())

    val coords = laneDetection.coords

    // ★ ByteTrack tracks 수집 (ID/정규화 bbox)
    val tracks by remember(service) {
        service?.tracksFlow() ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var showDetectionOverlay by rememberSaveable { mutableStateOf(true) }
    var showTrackingOverlay by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    BackHandler(enabled = uiState.isFullscreen) {
        viewModel.toggleFullscreen()
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
                    }

                    uiState.isLoading -> CameraLoadingScreen()

                    uiState.isDetectionActive && uiState.isCameraReady -> {
                        // ✅ 미리보기 + 오버레이
                        Box(modifier = Modifier.fillMaxSize()) {
                            CameraPreviewSection(
                                isDetectionActive = uiState.isDetectionActive,
                                isFullscreen = uiState.isFullscreen,
                                onToggleFullscreen = {
                                    viewModel.toggleFullscreen()
                                },
                                isDetectionOn = showDetectionOverlay,
                                isTrackingOn = showTrackingOverlay,
                                onToggleDetection = {
                                    showDetectionOverlay = !showDetectionOverlay
                                },
                                onToggleTracking = {
                                    showTrackingOverlay = !showTrackingOverlay
                                },
                                onServiceConnected = {svc -> service = svc}
                            )

                            if (showDetectionOverlay) {
                                DetectionOverlay(
                                    detections = detections,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            if (showTrackingOverlay){
                                TrackingOverlay(
                                    tracks = tracks,
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                        }
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
                    isActive = uiState.isDetectionActive,
                    selectedFilter = uiState.selectedViolationFilter,
                    violations = uiState.filteredViolations,
                    onFilterSelected = viewModel::onViolationFilterSelected,
                    onViolationClick = { violation -> viewModel.onViolationClick(violation) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
        FloatingActionButton(
            onClick = {
                viewModel.navigateViolationDetail(null)
                Log.d("FAB", "clicked")
            },
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
