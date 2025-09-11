package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportItem
import com.sos.chakhaeng.presentation.theme.BackgroundGray

@Composable
fun ReportListSection(
    reportList: List<ReportItem>,
    modifier: Modifier = Modifier,
    onItemClick: (ReportItem) -> Unit = {},
    onDelete: (ReportItem) -> Unit = {},
) {

    Box(
        modifier = Modifier
            .background(color = BackgroundGray),
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reportList,
                key = { it.id }
            ) { reportItem ->
                AnimatedVisibility(
                    visible = true,
                    exit = fadeOut(animationSpec = tween(300)) +
                            shrinkVertically(animationSpec = tween(300))
                ) {
                    ReportItemCard(
                        reportItem = reportItem,
                        onClick = { onItemClick(reportItem) },
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}