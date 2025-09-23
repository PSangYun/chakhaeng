package com.sos.chakhaeng.presentation.ui.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.presentation.theme.NEUTRAL100
import com.sos.chakhaeng.presentation.theme.NEUTRAL400
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.naverGreen
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun MissionInfoSection(
    missions: List<Mission>,
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
                    color = primaryLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 미션 리스트
            missions.take(missions.size).forEach { mission ->
                MissionItem(
                    mission = mission,
                )
                if (mission != missions.take(missions.size).last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun MissionItem(
    mission: Mission,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, NEUTRAL100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 미션 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = mission.iconRes),
                    contentDescription = mission.title,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF5E72E4)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 미션 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mission.title,
                    style = chakhaengTypography().bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mission.description,
                    style = chakhaengTypography().bodySmall,
                    color = NEUTRAL400
                )
            }

            // 완료 여부
            if (mission.isCompleted) {
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "완료",
                            style = chakhaengTypography().bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = naverGreen,
                    ),
                    border = null
                )
            } else {
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "진행중",
                            style = chakhaengTypography().bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = NEUTRAL100,
                    ),
                    border = null
                )
            }
        }
    }
}