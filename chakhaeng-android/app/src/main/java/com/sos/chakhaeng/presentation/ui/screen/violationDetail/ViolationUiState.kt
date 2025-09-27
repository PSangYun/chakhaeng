package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import com.sos.chakhaeng.data.network.dto.response.violation.UploadUrl
import com.sos.chakhaeng.domain.model.violation.ViolationEntity

data class ViolationDetailUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val lastUploaded: UploadUrl? = null,
    val violationDetail: ViolationEntity = ViolationEntity(),
    val videoObjectKey : String? = null,
    val videoId : String? = null,
    val showSubmitDialog : Boolean = false
//    val photoUrls: List<String> = emptyList()
)

val VIOLATION_TYPE_OPTIONS = listOf(
    "신호위반", "차선침범", "역주행", "킥보드 2인이상", "무번호판", "헬멧 미착용", "헬멧 미착용·중앙선 침범", "킥보드 2인이상·헬멧 미착용", "위반"
)
