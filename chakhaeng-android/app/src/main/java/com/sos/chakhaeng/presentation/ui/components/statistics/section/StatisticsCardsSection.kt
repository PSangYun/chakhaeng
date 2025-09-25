package com.sos.chakhaeng.presentation.ui.components.statistics.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.ui.components.statistics.StatCard
import java.util.Locale

@Composable
fun StatisticsCardsSection(
    totalDetections: Int,
    accuracy: Int,
    weeklyDetections: Int,
    dailyAverage: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "나의 탐지 횟수",
                value = totalDetections.toString(),
                iconVector = painterResource(R.drawable.ic_search_gd),
                iconColor = Color.Unspecified
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "전체 사용자 탐지 횟수",
                value = accuracy.toString(),
                iconVector = painterResource(R.drawable.ic_target_gd),
                iconColor = Color.Unspecified
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "이번 주 탐지 \n",
                value = weeklyDetections.toString(),
                iconVector = painterResource(R.drawable.ic_plan_gd),
                iconColor = Color.Unspecified
            )

            StatCard(
                modifier = Modifier.weight(1f),
                title = "사용자 평균 탐지",
                value = String.format(Locale.getDefault(), "%.1f", dailyAverage),
                iconVector = painterResource(R.drawable.ic_24hours_gd),
                iconColor = Color.Unspecified
            )
        }
    }
}