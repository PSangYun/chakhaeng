package com.sos.chakhaeng.presentation.ui.components.statistics.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.statistics.StatisticsTab
import com.sos.chakhaeng.presentation.ui.components.statistics.TabButton

@Composable
fun StatisticsTabSection(
    selectedTab: StatisticsTab,
    onTabSelected: (StatisticsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "위반 탐지 통계",
            isSelected = selectedTab == StatisticsTab.VIOLATION_STATISTICS,
            onClick = { onTabSelected(StatisticsTab.VIOLATION_STATISTICS) },
            modifier = Modifier.weight(1f)
        )

        TabButton(
            text = "신고 통계",
            isSelected = selectedTab == StatisticsTab.REPORT_STATISTICS,
            onClick = { onTabSelected(StatisticsTab.REPORT_STATISTICS) },
            modifier = Modifier.weight(1f)
        )
    }
}