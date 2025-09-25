package com.sos.chakhaeng.presentation.ui.components.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.theme.*
import java.util.Locale

@Composable
fun MissionProgressSection(
    completedCount: Int,
    totalCount: Int,
    progressPercentage: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = primaryContainerLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "미션 진행 현황",
                    style = chakhaengTypography().titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onPrimaryContainerLight
                )

                // 완료 개수 표시
                Surface(
                    color = backgroundLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        style = chakhaengTypography().bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryLight,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 진행률 바 섹션
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 진행률 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(backgroundLight.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressPercentage / 100f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryLight)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", progressPercentage)}% 완료",
                        style = chakhaengTypography().bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onPrimaryContainerLight
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 진행률에 따른 아이콘
                        Icon(
                            painter = painterResource(
                                id = when {
                                    progressPercentage >= 100f -> R.drawable.ic_trophy
                                    progressPercentage >= 75f -> R.drawable.ic_star
                                    progressPercentage >= 50f -> R.drawable.ic_power
                                    progressPercentage > 0f -> R.drawable.ic_rocket
                                    else -> R.drawable.ic_feeding_bottle
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Unspecified
                        )

                        Text(
                            text = when {
                                progressPercentage >= 100f -> "모든 미션 완료!"
                                progressPercentage >= 75f -> "거의 다 완료했어요!"
                                progressPercentage >= 50f -> "절반 이상 진행!"
                                progressPercentage > 0f -> "미션 진행 중!"
                                else -> "미션을 시작해보세요!"
                            },
                            style = chakhaengTypography().bodySmall,
                            color = onPrimaryContainerLight.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}