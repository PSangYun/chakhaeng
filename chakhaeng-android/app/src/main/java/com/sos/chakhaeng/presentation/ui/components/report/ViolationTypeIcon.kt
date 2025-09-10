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
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.ViolationType

@Composable
fun ViolationTypeIcon(
    violationType: ViolationType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(violationType.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (violationType) {
                ViolationType.ALL -> Icons.Default.Check
                ViolationType.WRONG_WAY -> Icons.Default.Warning
                ViolationType.SIGNAL -> Icons.Default.Traffic
                ViolationType.LANE -> Icons.Default.SwapHoriz
                ViolationType.NO_PLATE -> Icons.Default.CreditCard
                ViolationType.NO_HELMET -> Icons.Default.Security
                ViolationType.OTHERS -> Icons.Default.Error
            },
            contentDescription = violationType.displayName,
            tint = violationType.iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}