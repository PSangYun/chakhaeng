package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.ViolationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViolationDetailViewModel @Inject constructor(
): ViewModel() {

    private val _uiState = MutableStateFlow(ViolationDetailUiState())
    val uiState: StateFlow<ViolationDetailUiState> = _uiState

    fun load(entity: ViolationEntity) {
        _uiState.value = ViolationDetailUiState(
            reportType = entity.reportType,
            region = entity.region,
            title = entity.title,
            content = entity.content,
            carNumber = entity.carNumber,
            date = entity.date,
            time = entity.time,
            videoThumbnailUrl = entity.videoThumbnailUrl,
            videoUrl = entity.videoUrl,
            photoUrls = emptyList() // 필요 시 서버 데이터와 매핑
        )
    }

    fun toggleEdit(onSave: (ViolationDetailUiState) -> Unit) {
        _uiState.update { cur ->
            val next = cur.copy(isEditing = !cur.isEditing)
            if (cur.isEditing && !next.isEditing) onSave(next)
            next
        }
    }

    // 편집 필드 업데이트
    fun updateReportType(v: String) = _uiState.update { it.copy(reportType = v) }
    fun updateRegion(v: String)     = _uiState.update { it.copy(region = v) }
    fun updateTitle(v: String)      = _uiState.update { it.copy(title = v) }
    fun updateContent(v: String)    = _uiState.update { it.copy(content = v) }
    fun updateCarNumber(v: String)  = _uiState.update { it.copy(carNumber = v) }
    fun updateDate(v: String)       = _uiState.update { it.copy(date = v) }
    fun updateTime(v: String)       = _uiState.update { it.copy(time = v) }

    // 서버에서 상세(동영상 URL 포함) 다시 받아올 때 호출 예시
    fun refreshFromServer(fetch: suspend () -> ViolationEntity) {
        viewModelScope.launch {
            val entity = fetch()
            load(entity)
        }
    }
}