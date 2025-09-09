package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import com.sos.chakhaeng.domain.model.ViolationEntity

data class ViolationDetailUiState(
    val isEditing: Boolean = false,
    val reportType: String = "",
    val region: String = "",
    val title: String = "",
    val content: String = "",
    val carNumber: String = "",
    val date: String = "",
    val time: String = "",
    val videoThumbnailUrl: String? = null,
    val videoUrl: String? = null,             // ✅ 추가
    val photoUrls: List<String> = emptyList()
)

val VIOLATION_TYPE_OPTIONS = listOf(
    "신호위반", "차선침범", "역주행", "무번호판", "헬멧 미착용", "위반"
)
