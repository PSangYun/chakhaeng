package com.sos.chakhaeng.presentation.ui.components.allbadges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.theme.backgroundLight
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onPrimaryContainerLight
import com.sos.chakhaeng.presentation.theme.primaryContainerLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import java.util.Locale

@Composable
fun BadgeProgressSection(
    unlockedCount: Int,
    totalCount: Int,
    progressPercentage: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryContainerLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "배지 수집 현황",
                    style = chakhaengTypography().titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onPrimaryContainerLight
                )

                // 획득 개수 표시
                Surface(
                    color = backgroundLight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "$unlockedCount/$totalCount",
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
                        text = "${String.format(Locale.KOREA, "%.1f", progressPercentage)}% 완료",
                        style = chakhaengTypography().bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onPrimaryContainerLight
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = when {
                                    progressPercentage >= 100f -> R.drawable.ic_trophy
                                    progressPercentage >= 75f -> R.drawable.ic_star
                                    progressPercentage >= 50f -> R.drawable.ic_power
                                    progressPercentage > 0f -> R.drawable.ic_feeding_bottle
                                    else -> R.drawable.ic_rocket
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Unspecified
                        )

                        Text(
                            text = when {
                                progressPercentage >= 100f -> "모든 배지 수집 완료!"
                                progressPercentage >= 75f -> "거의 다 모았어요!"
                                progressPercentage >= 50f -> "절반 이상 수집!"
                                progressPercentage > 0f -> "좋은 시작이에요!"
                                else -> "배지 수집을 시작해보세요!"
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
