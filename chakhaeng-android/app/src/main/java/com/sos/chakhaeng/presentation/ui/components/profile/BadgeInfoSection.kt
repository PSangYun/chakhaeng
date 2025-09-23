package com.sos.chakhaeng.presentation.ui.components.profile

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.theme.NEUTRAL100
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onPrimaryContainerLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.tertiaryLight
import com.sos.chakhaeng.presentation.ui.components.angledLinearGradientBackground

@Composable
fun BadgeInfoSection(
    badges: List<Badge>,
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
                    color = primaryLight
                )
            }


            // 배지 리스트

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(badges) { badge ->
                    BadgeItem(
                        badge = badge
                    )
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
                    Color(0xFFfff0f5), // 연한 핑크
                    Color(0xFFe8f4f8), // 연한 민트
                    Color(0xFFf0f8ff).copy(alpha = 0.5f) // 연한 하늘
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
            // 배지 아이콘
            Image(
                painter = painterResource(badge.iconRes),
                contentDescription = badge.name,
                modifier = Modifier.size(120.dp),
            )

            // 배지 이름
            Text(
                text = badge.name,
                style = chakhaengTypography().bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (badge.isUnlocked) {
                    Color.Black.copy(0.6f)
                } else {
                    NEUTRAL200
                },
                maxLines = 1
            )
        }
    }
    
}