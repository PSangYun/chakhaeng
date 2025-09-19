package com.sos.chakhaeng.presentation.ui.components.statistics.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.statistics.HourlyStatistic
import com.sos.chakhaeng.presentation.ui.components.statistics.chart.HourlyBarChart

@Composable
fun HourlyDistributionSection(
    hourlyStats: List<HourlyStatistic>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            HourlyBarChart(
                data = hourlyStats,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
