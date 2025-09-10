package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.theme.lightGray
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight

@Composable
fun ReportListSection(
    reportList: List<ReportItem>,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    onItemClick: (ReportItem) -> Unit = {},
    onWatchVideo: (ReportItem) -> Unit = {},
    onDelete: (ReportItem) -> Unit = {},
) {

    Box(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .background(color = BackgroundGray),
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reportList,
                key = { it.id }
            ) { reportItem ->
                ReportItemCard(
                    reportItem = reportItem,
                    onClick = { onItemClick(reportItem) },
                    onWatchVideo = onWatchVideo,
                    onDelete = onDelete
                )
            }
        }
    }
}