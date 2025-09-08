package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.ui.model.ViolationType

@Composable
fun ViolationFilterChips(
    selectedFilter: ViolationType,
    onFilterSelected: (ViolationType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        ViolationType.ALL,
        ViolationType.TRAFFIC_LIGHT_VIOLATION,
        ViolationType.SPEED_VIOLATION,
        ViolationType.LANE_VIOLATION,
        ViolationType.CUTTING_IN
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters) { filter ->
            FilterChip(
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                selected = selectedFilter == filter,
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}