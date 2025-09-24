package com.sos.chakhaeng.presentation.ui.components.mission

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.presentation.theme.*
import java.util.Locale

@Composable
fun MissionItem(
    mission: Mission,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundLight
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더 (아이콘, 제목, 상태)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 미션 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (mission.isCompleted) {
                                lightGreen
                            } else {
                                primaryContainerLight
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(mission.iconRes),
                        contentDescription = mission.title,
                        modifier = Modifier
                            .size(60.dp)
                            .let { imageModifier ->
                                if (!mission.isCompleted) {
                                    imageModifier.alpha(0.4f)
                                } else {
                                    imageModifier
                                }
                            },
                    )
                }

                // 제목과 설명
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = mission.title,
                        style = chakhaengTypography().titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onBackgroundLight
                    )
                    Text(
                        text = mission.description,
                        style = chakhaengTypography().bodySmall,
                        color = onSurfaceVariantLight,
                        maxLines = 2
                    )
                }

                // 상태 표시
                if (mission.isCompleted) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "완료",
                                style = chakhaengTypography().bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = backgroundLight
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = naverGreen
                        ),
                        border = null
                    )
                } else {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${
                                    String.format(
                                        Locale.US,
                                        "%.0f",
                                        mission.progressPercentage
                                    )
                                }%",
                                style = chakhaengTypography().bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = primaryContainerLight
                        ),
                        border = null
                    )
                }
            }

            // 진행률 바
            if (!mission.isCompleted) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 진행률 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${mission.currentProgress}/${mission.targetProgress}",
                            style = chakhaengTypography().bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceVariantLight
                        )

                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(NEUTRAL100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(mission.progressPercentage / 100f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        mission.progressPercentage >= 80f -> naverGreen
                                        mission.progressPercentage >= 50f -> primaryLight
                                        else -> ORANGE
                                    }
                                )
                        )
                    }
                }
            } else {
                // 완료된 미션의 보상 표시
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${mission.rewardName} 획득 완료",
                        style = chakhaengTypography().bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = naverGreen
                    )
                }
            }
        }
    }
}