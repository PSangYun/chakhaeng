package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.errorLight
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ReportDetailInfoSection(
    reportDetailItem: ReportDetailItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "위반정보",
                style = chakhaengTypography().titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "위반 유형",
                    style = chakhaengTypography().bodyMedium,
                )
                Text(
                    text = reportDetailItem.violationType.displayName,
                    style = chakhaengTypography().bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = errorLight
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "차량 번호",
                    style = chakhaengTypography().bodyMedium,
                )
                Text(
                    text = reportDetailItem.plateNumber,
                    style = chakhaengTypography().bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "발생 시간",
                    style = chakhaengTypography().bodyMedium,
                )
                Text(
//                    text = formatTimestamp(reportDetailItem.occurredAt),
                    text = "2024-01-15 14:30",
                    style = chakhaengTypography().bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "발생 위치",
                    style = chakhaengTypography().bodyMedium,
                )
                Text(
                    text = reportDetailItem.location,
                    style = chakhaengTypography().bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatTimestamp(epochMilli: Long): String {
    val instant = Instant.ofEpochMilli(epochMilli)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return localDateTime.format(formatter)
}