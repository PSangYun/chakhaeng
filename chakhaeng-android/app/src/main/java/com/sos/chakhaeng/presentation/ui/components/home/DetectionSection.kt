package com.sos.chakhaeng.presentation.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun DetectionSection(
    isDetectionActive: Boolean,
    onDetectionAction: () -> Unit,
    showDialog: Boolean,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDetectionActive) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            } else {
                primaryLight
            }
        )
    ) {
        DetectionContent(
            isDetectionActive = isDetectionActive,
            onDetectionAction = onDetectionAction
        )
    }

    if (showDialog) {
        DetectionDialog(
            isDetectionActive = isDetectionActive,
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss
        )
    }
}

@Composable
private fun DetectionContent(
    isDetectionActive: Boolean,
    onDetectionAction: () -> Unit
) {
    Button(
        onClick = onDetectionAction,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDetectionActive) {
                MaterialTheme.colorScheme.error
            } else {
                Color.White.copy(alpha = 0.2f)
            }
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isDetectionActive) {
                Image(
                    painter = painterResource(id = R.drawable.ic_stop_filled),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_eye_unfilled),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            Text(
                text = if (isDetectionActive) "위반 탐지 종료" else "위반 탐지 시작",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isDetectionActive) {
                    "탐지를 중단하고 대기 상태로 전환됩니다."
                } else {
                    "AI가 실시간으로 교통위반을 탐지합니다"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
