package com.sos.chakhaeng.presentation.ui.components.allbadges

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.BadgeColors
import com.sos.chakhaeng.presentation.theme.BadgeGradientColors
import com.sos.chakhaeng.presentation.theme.backgroundLight
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onBackgroundLight
import com.sos.chakhaeng.presentation.ui.components.angledLinearGradientBackground

@Composable
fun AllBadgeItem(
    badge: Badge,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBadgeClick(badge) },
        colors = CardDefaults.cardColors(
            containerColor = backgroundLight
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 4.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .let { mod ->
                    if (badge.isUnlocked) {
                        mod.angledLinearGradientBackground(
                            colors = listOf(
                                BadgeGradientColors.Pink,
                                BadgeGradientColors.Mint,
                                BadgeGradientColors.Sky.copy(alpha = 0.5f)
                            ),
                            angleDeg = 45f,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        mod.background(
                            color = BadgeColors.LockedBackground,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 배지 아이콘
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(badge.iconRes?: 0),
                        contentDescription = badge.name,
                        modifier = Modifier
                            .size(60.dp)
                            .let { imageModifier ->
                                if (!badge.isUnlocked) {
                                    imageModifier.alpha(0.4f)
                                } else {
                                    imageModifier
                                }
                            },
                    )
                }

                // 배지 이름
                Text(
                    text = badge.name,
                    style = chakhaengTypography().bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (badge.isUnlocked) {
                        onBackgroundLight.copy(0.8f)
                    } else {
                        NEUTRAL200
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                // 상태 표시
                if (badge.isUnlocked) {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "획득 완료",
                                style = chakhaengTypography().bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = backgroundLight
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = BadgeColors.UnlockedChip
                        ),
                        border = null
                    )
                } else {
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "미획득",
                                style = chakhaengTypography().bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = BadgeColors.LockedChip
                        ),
                        border = null
                    )
                }
            }
        }
    }
}