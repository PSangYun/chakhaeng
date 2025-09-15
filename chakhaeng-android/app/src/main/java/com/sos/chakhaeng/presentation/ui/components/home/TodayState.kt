package com.sos.chakhaeng.presentation.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.home.TodayStats
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.neutral
import com.sos.chakhaeng.presentation.theme.primaryDark

@Composable
fun TodayState(
    todayStats: TodayStats,
    modifier: Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.magnifying_glasses2),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${todayStats.todayDetectionCnt}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )
            Text(
                text = "오늘 탐지",
                style = chakhaengTypography().labelLarge,
                color = Color(0xFF0B648D)
            )
        }
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 2.dp,
            color = neutral
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.siren),
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${todayStats.todayReportCnt}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )
            Text(
                text = "신고 완료",
                style = chakhaengTypography().labelLarge,
                color = Color(0xFFC71A0F)
            )
        }
    }
}
