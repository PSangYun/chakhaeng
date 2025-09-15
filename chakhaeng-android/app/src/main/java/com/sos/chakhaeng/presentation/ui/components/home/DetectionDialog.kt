package com.sos.chakhaeng.presentation.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.NEUTRAL400
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
import com.sos.chakhaeng.presentation.theme.RED300
import com.sos.chakhaeng.presentation.theme.errorContainerLightMediumContrast
import com.sos.chakhaeng.presentation.theme.errorLight
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun DetectionDialog(
    isDetectionActive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = if (isDetectionActive) {
                            errorLight.copy(alpha = 0.1f)
                        } else {
                            primaryLight.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDetectionActive) {
                        Icons.Default.Stop
                    } else {
                        Icons.Default.Videocam
                    },
                    contentDescription = null,
                    tint = if (isDetectionActive) {
                        errorLight
                    } else {
                        primaryLight
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isDetectionActive) {
                    "위반 탐지를 종료하시겠습니까?"
                } else {
                    "위반 탐지를 시작하시겠습니까?"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = NEUTRAL800,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        },
        text = {
            Column {
                Text(
                    text = if (isDetectionActive) {
                        "탐지를 중단하고 대기 상태로 전환합니다."
                    } else {
                        "AI가 실시간으로 교통법규 위반을 탐지합니다."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = NEUTRAL400,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("취소")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDetectionActive) {
                            RED300
                        } else {
                            primaryLight
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isDetectionActive) "종료" else "확인",
                        color = if (isDetectionActive) Color.White else Color.Black
                    )
                }
            }
        },
        dismissButton = {}
    )
}