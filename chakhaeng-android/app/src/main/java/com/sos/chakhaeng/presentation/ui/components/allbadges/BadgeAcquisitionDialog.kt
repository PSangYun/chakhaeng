package com.sos.chakhaeng.presentation.ui.components.allbadges

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.presentation.theme.*

@Composable
fun BadgeAcquisitionDialog(
    badge: Badge?,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible || badge == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundLight
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 배지 아이콘 섹션
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (badge.isUnlocked) {
                                BadgeColors.DialogUnlockedBackground
                            } else {
                                BadgeColors.DialogLockedBackground
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(badge.iconRes?: 0),
                        contentDescription = badge.name,
                        modifier = Modifier
                            .size(80.dp)
                    )
                }

                // 배지 정보 섹션
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = badge.name,
                        style = chakhaengTypography().titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = onBackgroundLight
                    )

                    Text(
                        text = badge.description,
                        style = chakhaengTypography().bodyMedium,
                        color = onSurfaceVariantLight,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = chakhaengTypography().bodyMedium.lineHeight * 1.2,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .heightIn(min = 40.dp)
                    )
                }

                // 상태별 메시지 카드
                if (badge.isUnlocked) {
                    // 획득 완료 메시지
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BadgeColors.DialogSuccessBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_confetti),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "축하합니다!",
                                    style = chakhaengTypography().titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = BadgeColors.DialogSuccessText
                                )
                                Text(
                                    text = "이미 획득한 배지입니다.",
                                    style = chakhaengTypography().bodySmall,
                                    color = BadgeColors.DialogSuccessText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    // 획득 방법 안내 메시지
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BadgeColors.DialogGuideBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_light_bulb),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified
                                )
                                Text(
                                    text = "배지 획득 방법",
                                    style = chakhaengTypography().titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = BadgeColors.DialogGuideTitle
                                )
                            }

                            Text(
                                text = "다양한 미션을 완료하고 활동을 통해 배지를 획득할 수 있습니다. 미션 화면에서 진행 상황을 확인해보세요!",
                                style = chakhaengTypography().bodySmall,
                                color = BadgeColors.DialogGuideText,
                                lineHeight = chakhaengTypography().bodySmall.lineHeight * 1.2
                            )
                        }
                    }
                }

                // 버튼 섹션
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 닫기 버튼
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (badge.isUnlocked) {
                                BadgeColors.UnlockedButton
                            } else {
                                NEUTRAL400
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "확인",
                            style = chakhaengTypography().labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = backgroundLight,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}