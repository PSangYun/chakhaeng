package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.utils.OccurredAtFormatter
import com.sos.chakhaeng.data.network.dto.response.violation.UploadUrl
import com.sos.chakhaeng.domain.model.violation.ViolationEntity
import com.sos.chakhaeng.domain.usecase.video.GetStreamingVideoUrlUseCase
import com.sos.chakhaeng.domain.usecase.video.UploadVideoUseCase
import com.sos.chakhaeng.domain.usecase.violation.GetViolationDetailUseCase
import com.sos.chakhaeng.domain.usecase.violation.SubmitViolationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class ViolationDetailViewModel @Inject constructor(
    private val navigator: Navigator,
    private val submitViolationUseCase: SubmitViolationUseCase,
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val getViolationDetailUseCase: GetViolationDetailUseCase,
    private val getStreamingVideoUrlUseCase: GetStreamingVideoUrlUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(ViolationDetailUiState())
    val uiState: StateFlow<ViolationDetailUiState> = _uiState

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()
    fun showSubmitDialog(bool : Boolean){
        _uiState.update {
            it.copy(
                showSubmitDialog = bool
            )
        }
    }
    fun onSubmit() {
        Log.d("TAG", "onSubmit: 버튼 클릭")
        val entity = uiState.value.violationDetail
        viewModelScope.launch {
            Log.d("TAG", "onSubmit: 버튼 클릭22")
            submitViolationUseCase(entity)
                .onSuccess { resp ->
                    _event.emit("신고가 접수되었습니다.")
                    navigator.navigateBack()
                }
                .onFailure { e ->
                    _event.emit( "신고 접수 중 오류가 발생했습니다.")
                }
        }
    }

    fun load(violationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getViolationDetailUseCase(violationId)
                .onSuccess { detail ->
                    val (dateStr, timeStr) = OccurredAtFormatter.split(detail.occurredAt/*, ZoneId.of("Asia/Seoul")*/)
                    _uiState.value = _uiState.value.copy(
                        violationDetail = _uiState.value.violationDetail.copy(
                            violationType = detail.type,
                            title = "${detail.locationText}에서 ${detail.type}",
                            description = "${detail.locationText}에서 $dateStr : $timeStr 에 ${detail.type} 감지 되었습니다",
                            location = detail.locationText,
                            plateNumber = detail.plate,
                            date = dateStr,
                            time = timeStr,
                            videoUrl = detail.videoId
                        ),
                        videoId = detail.videoId,
                        videoObjectKey = detail.objectKey
                    )
                }
                .onFailure { e ->
                    _uiState.update { s -> s.copy(isLoading = false) }
                    _event.emit(e.message ?: "상세 정보를 불러오지 못했습니다.")
                }
        }
    }

    fun popBackStack(){
        viewModelScope.launch {
            navigator.navigateBack()
        }
    }
    private inline fun updateEntity(block: ViolationEntity.() -> ViolationEntity) {
        _uiState.update { st -> st.copy(violationDetail = st.violationDetail.block()) }
    }

    fun toggleEdit(onSave: (ViolationDetailUiState) -> Unit) {
        Log.d("TAG", "toggleEdit: ${uiState.value.isEditing}")
        _uiState.update { cur ->
            val next = cur.copy(isEditing = !cur.isEditing)
            if (cur.isEditing && !next.isEditing) onSave(next)
            next
        }
        Log.d("TAG", "toggleEdit: ${uiState.value.isEditing}")
    }

    fun updateViolationType(v: String)     = updateEntity { copy(violationType = v) }
    fun updateLocation(v: String)          = updateEntity { copy(location = v) }
    fun updateTitle(v: String)             = updateEntity { copy(title = v) }
    fun updateDescription(v: String)       = updateEntity { copy(description = v) }
    fun updatePlateNumber(v: String)       = updateEntity { copy(plateNumber = v) }
    fun updateDate(v: String)              = updateEntity { copy(date = v) }
    fun updateTime(v: String)              = updateEntity { copy(time = v) }


    fun onVideoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0f) }
            uploadVideoUseCase(uri) { sent, total ->
                val p = if (total != null && total > 0) sent.toFloat() / total else Float.NaN
                _uiState.update { s -> s.copy(uploadProgress = p) }
            }.onSuccess { data ->
                _uiState.update { s ->
                    s.copy(
                        isUploading = false,
                        uploadProgress = 1f,
                        lastUploaded = data.uploadUrl,
                        violationDetail = s.violationDetail.copy(videoUrl = data.complete.id)
                    )
                }
            }.onFailure {
                _uiState.update { s -> s.copy(isUploading = false) }
                _event.emit("동영상 업로드 중 오류가 발생했습니다.")
            }
        }
    }
    fun loadVideo(objectKey: String?) {
        if (objectKey == null) return

        viewModelScope.launch {
            runCatching { getStreamingVideoUrlUseCase(objectKey) }
                .onSuccess { url ->
                    _uiState.update { it.copy(lastUploaded = UploadUrl(url.url)) }
                }
        }
    }

}