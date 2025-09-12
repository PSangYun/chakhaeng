package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.usecase.violation.SubmitViolationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViolationDetailViewModel @Inject constructor(
    private val submitViolationUseCase: SubmitViolationUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(ViolationDetailUiState())
    val uiState: StateFlow<ViolationDetailUiState> = _uiState

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    fun onSubmit() {
        Log.d("TAG", "onSubmit: 버튼 클릭")
        val entity = uiState.value.violationDetail
        viewModelScope.launch {
            Log.d("TAG", "onSubmit: 버튼 클릭22")
            submitViolationUseCase(entity)
                .onSuccess { resp ->
                    _event.emit("신고가 접수되었습니다. (id=${resp.id})")
                }
                .onFailure { e ->
                    _event.emit(e.message ?: "신고 접수 중 오류가 발생했습니다.")
                }
        }
    }

    private inline fun updateEntity(block: ViolationEntity.() -> ViolationEntity) {
        _uiState.update { st -> st.copy(violationDetail = st.violationDetail.block()) }
    }

    fun toggleEdit(onSave: (ViolationDetailUiState) -> Unit) {
        _uiState.update { cur ->
            val next = cur.copy(isEditing = !cur.isEditing)
            if (cur.isEditing && !next.isEditing) onSave(next)
            next
        }
    }

    // 편집 필드 업데이트
    fun updateViolationType(v: String)     = updateEntity { copy(violationType = v) }
    fun updateLocation(v: String)          = updateEntity { copy(location = v) }
    fun updateTitle(v: String)             = updateEntity { copy(title = v) }
    fun updateDescription(v: String)       = updateEntity { copy(description = v) }
    fun updatePlateNumber(v: String)       = updateEntity { copy(plateNumber = v) }
    fun updateDate(v: String)              = updateEntity { copy(date = v) }
    fun updateTime(v: String)              = updateEntity { copy(time = v) }
    fun updateVideoUrl(url: String)       = updateEntity { copy(videoUrl = url) }


    fun onVideoSelected(uri: Uri) {
        viewModelScope.launch {
//            // TODO: 업로드 진행 표시 (로딩 상태)
//            repository.uploadViolationVideo(uri)
//                .onSuccess { newUrl ->
//                    // TODO: 상태 갱신
//                    // uiState = uiState.copy(videoUrl = newUrl, …)
//                }
//                .onFailure { e ->
//                    // TODO: 에러 처리(토스트/스낵바/상태)
//                }
        }
    }

    fun deleteVideo() {
        viewModelScope.launch {
//            repository.deleteViolationVideo()
//                .onSuccess {
//                    // uiState = uiState.copy(videoUrl = "")
//                }
        }
    }


    // 서버에서 상세(동영상 URL 포함) 다시 받아올 때 호출 예시
    fun refreshFromServer(fetch: suspend () -> ViolationEntity) {
        viewModelScope.launch {
            val entity = fetch()
            updateEntity({ entity })
        }
    }
}