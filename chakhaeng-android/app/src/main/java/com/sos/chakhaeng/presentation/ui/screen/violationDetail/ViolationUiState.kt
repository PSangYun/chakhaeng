package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import com.sos.chakhaeng.data.network.dto.request.violation.UploadResult
import com.sos.chakhaeng.data.network.dto.response.violation.CompleteUploadResponse
import com.sos.chakhaeng.domain.model.violation.ViolationEntity

data class ViolationDetailUiState(
    val isEditing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val lastUploaded: UploadResult? = null,
    val violationDetail: ViolationEntity = ViolationEntity(),
//    val photoUrls: List<String> = emptyList()
)

val VIOLATION_TYPE_OPTIONS = listOf(
    "신호위반", "차선침범", "역주행", "무번호판", "헬멧 미착용", "위반"
)
