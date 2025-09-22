// domain/models/ViolationType.kt
package com.sos.chakhaeng.domain.model

import androidx.compose.ui.graphics.Color
import com.sos.chakhaeng.presentation.theme.ViolationColors

enum class ViolationType(
    val displayName: String,
    val backgroundColor: Color,
    val iconColor: Color  // 아이콘 색상 추가
) {
    ALL("전체", ViolationColors.All, ViolationColors.AllIcon),
    WRONG_WAY("역주행", ViolationColors.Critical, ViolationColors.CriticalIcon),
    SIGNAL("신호위반", ViolationColors.High, ViolationColors.HighIcon),
    LANE("차선침범", ViolationColors.Medium, ViolationColors.MediumIcon),
    NO_PLATE("무번호판", ViolationColors.LowAmber, ViolationColors.LowAmberIcon),
    NO_HELMET("헬멧 미착용", ViolationColors.LowPurple, ViolationColors.LowPurpleIcon),
    OTHERS("기타", ViolationColors.Others, ViolationColors.OthersIcon)
}