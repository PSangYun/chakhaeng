package com.sos.chakhaeng.presentation.ui.components.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.BadgeGradientColors
import com.sos.chakhaeng.presentation.theme.backgroundLight
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onBackgroundLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.components.angledLinearGradientBackground
import okhttp3.internal.filterList

@Composable
fun BadgeInfoSection(
    badges: List<Badge>,
    onAllBadgesClick: () -> Unit, // 전체 배지 클릭 콜백 추가
    modifier: Modifier = Modifier
) {
    val validBadgeList = badges.filter {
        it.isUnlocked == true
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "보유 배지",
                    style = chakhaengTypography().titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "전체 배지",
                    style = chakhaengTypography().bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryLight,
                    modifier = Modifier.clickable { onAllBadgesClick() }
                )
            }

            // 배지 리스트
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (validBadgeList.isEmpty()) {
                    item {
                        Text(
                            text = "아직 보유한 배지가 없습니다.",
                            style = chakhaengTypography().bodyMedium,
                            color = NEUTRAL200
                        )
                    }
                } else{
                    items(validBadgeList) { badge ->
                        BadgeItem(
                            badge = badge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .angledLinearGradientBackground(
                colors = listOf(
                    BadgeGradientColors.Pink,
                    BadgeGradientColors.Mint,
                    BadgeGradientColors.Sky.copy(alpha = 0.5f)
                ),
                angleDeg = 45f,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = modifier
                .width(160.dp)
                .height(220.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Image(
                painter = painterResource(badge.iconRes?: 0),
                contentDescription = badge.name,
                modifier = Modifier.size(120.dp),
            )

            Text(
                text = badge.name,
                style = chakhaengTypography().bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) {
                    onBackgroundLight.copy(0.6f)
                } else {
                    NEUTRAL200
                },
                maxLines = 1
            )
        }
    }
}