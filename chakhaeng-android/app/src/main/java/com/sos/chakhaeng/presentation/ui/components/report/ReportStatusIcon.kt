package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportStatus

@Composable
fun ReportStatusIcon(
    reportState: ReportStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(reportState.textColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when(reportState) {
                ReportStatus.PROCESSING -> Icons.Default.WatchLater
                ReportStatus.COMPLETED -> Icons.Default.CheckCircle
                ReportStatus.REJECTED -> Icons.Default.Warning
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }
}