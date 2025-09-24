package com.sos.chakhaeng.presentation.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun MissionInfoSection(
    missions: List<Mission>,
    onMissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 달성 미션",
                    style = chakhaengTypography().titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "미션 보기",
                    style = chakhaengTypography().bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryLight,
                    modifier = Modifier.clickable { onMissionClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 미션 리스트
            missions.take(missions.size).forEach { mission ->
                MissionItemList(
                    mission = mission,
                )
                if (mission != missions.take(missions.size).last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}