package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportStatus
import com.sos.chakhaeng.presentation.theme.chakhaengTypography

@Composable
fun StatusChip(
    status: ReportStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = status.backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = chakhaengTypography().labelLarge,
            color = status.textColor,
            fontWeight = FontWeight.Bold
        )
    }
}