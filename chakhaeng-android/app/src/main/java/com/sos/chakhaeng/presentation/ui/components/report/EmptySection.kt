package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportTab
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight

@Composable
fun EmptySection(
    selectedTab: ReportTab,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = onSurfaceVariantLight
            )
            Text(
                text = when (selectedTab) {
                    ReportTab.ALL -> "신고 내역이 없습니다"
                    ReportTab.PROCESSING -> "처리 중인 신고가 없습니다"
                    ReportTab.COMPLETED -> "완료된 신고가 없습니다"
                    ReportTab.REJECTED -> "반려된 신고가 없습니다"
                },
                style = chakhaengTypography().titleMedium,
                color = onSurfaceVariantLight
            )
            Text(
                text = "교통 위반을 발견하시면 신고해주세요",
                style = chakhaengTypography().bodyMedium,
                color = onSurfaceVariantLight
            )
        }
    }
}