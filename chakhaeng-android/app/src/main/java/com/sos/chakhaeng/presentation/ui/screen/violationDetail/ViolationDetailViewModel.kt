package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.data.manager.VideoUploadManager
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
    private val submitViolationUseCase: SubmitViolationUseCase,
    private val videoUploadManager: VideoUploadManager
): ViewModel() {

    private val _uiState = MutableStateFlow(ViolationDetailUiState())
    var uiState by mutableStateOf(ViolationDetailUiState())
        private set

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    fun onSubmit() {
        Log.d("TAG", "onSubmit: 버튼 클릭")
        val entity = uiState.violationDetail
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
            uiState = uiState.copy(isUploading = true, uploadProgress = 0f)
            videoUploadManager.uploadVideo(uri) { sent, total ->
                val p = if (total != null && total > 0) sent.toFloat() / total else Float.NaN
                uiState = uiState.copy(uploadProgress = p)
            }.onSuccess { data ->
                uiState = uiState.copy(
                    isUploading = false,
                    uploadProgress = 1f,
                    lastUploaded = data,
                    violationDetail = uiState.violationDetail.copy(videoUrl = data.downloadUrl)
                )
            }.onFailure {
                uiState = uiState.copy(isUploading = false)
                _event.emit("동영상 업로드 중 오류가 발생했습니다.")
            }
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