package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun CameraStatusOverlay(
    isDetectionActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    isDetectionOn: Boolean,
    isTrackingOn: Boolean,
    onToggleDetection: () -> Unit,
    onToggleTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 탐지 상태 표시
        DetectionStatusCard(
            isDetectionActive = isDetectionActive,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            OverlayToggleChip(
                label = if (isDetectionOn) "객체 오버레이 ON" else "객체 오버레이 OFF",
                isOn = isDetectionOn,
                onToggle = onToggleDetection,
                modifier = Modifier
                    .padding(bottom = 8.dp).padding(horizontal = 16.dp)
            )

            OverlayToggleChip(
                label = if (isTrackingOn) "트래킹 ON" else "트래킹 OFF",
                isOn = isTrackingOn,
                onToggle = onToggleTracking,
                modifier = Modifier
                    .padding(bottom = 16.dp).padding(horizontal = 16.dp)
            )
        }

        // 전체화면 토글 버튼
        FullscreenToggleButton(
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DetectionStatusCard(
    isDetectionActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isDetectionActive) Color.Green else Color.Gray,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isDetectionActive) "탐지 중" else "대기 중",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FullscreenToggleButton(
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        )
    ) {
        IconButton(
            onClick = onToggleFullscreen,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isFullscreen) {
                    Icons.Default.FullscreenExit
                } else {
                    Icons.Default.Fullscreen
                },
                contentDescription = if (isFullscreen) "최소화" else "전체화면",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun OverlayToggleChip(
    label: String,
    isOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        Color.Black.copy(alpha = 0.60f),            // 검정(OFF)
        label = "overlay_chip_bg"
    )
    val dotColor by animateColorAsState(
        if (isOn) Color(0xFFE53935).copy(alpha = 0.90f)  else Color(0xFF9E9E9E),
        label = "overlay_chip_dot"
    )

    Card(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}