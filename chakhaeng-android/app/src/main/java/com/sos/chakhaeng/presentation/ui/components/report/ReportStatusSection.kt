package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.domain.model.report.ReportStatus
import com.sos.chakhaeng.presentation.theme.chakhaengTypography

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ReportStatusSection(
    reportDetailItem: ReportDetailItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = reportDetailItem.reportState.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ){
            ReportStatusIcon(
                reportState = reportDetailItem.reportState,
                modifier = Modifier.size(60.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = reportDetailItem.reportState.displayName,
                    style = chakhaengTypography().titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when(reportDetailItem.reportState) {
                        ReportStatus.PROCESSING ->  "신고 처리가 진행중입니다."
                        ReportStatus.COMPLETED ->  "신고 처리가 완료되었습니다."
                        ReportStatus.REJECTED ->  "신고 처리가 거절되었습니다."
                    },
                    style = chakhaengTypography().bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}