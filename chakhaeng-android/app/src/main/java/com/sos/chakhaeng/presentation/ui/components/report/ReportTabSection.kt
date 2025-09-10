package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportTab
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.lightGray
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun ReportTabSection(
    selectedTab: ReportTab,
    onTabSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportTab.values().forEach { tab ->
                FilterChip(
                    onClick = { onTabSelected(tab) },
                    label = {
                        Text(
                            text = tab.displayName,
                            style = chakhaengTypography().bodyMedium,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedTab == tab,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryLight,
                        selectedLabelColor = Color.White,
                    ),
                    border = if (selectedTab == tab) {
                        null
                    } else {
                        BorderStroke(
                            width = 1.dp,
                            color = lightGray
                        )
                    }
                )
            }
        }
    }
}