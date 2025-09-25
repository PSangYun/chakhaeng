package com.sos.chakhaeng.presentation.ui.screen.detectionDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.utils.OccurredAtFormatter
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.usecase.map.GetLocationFromAddressUseCase
import com.sos.chakhaeng.domain.usecase.video.GetStreamingVideoUrlUseCase
import com.sos.chakhaeng.domain.usecase.violation.GetViolationDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DetectionDetailViewModel @Inject constructor(
    private val navigator: Navigator,
    private val getLocationFromAddressUseCase: GetLocationFromAddressUseCase,
    private val getViolationDetailUseCase: GetViolationDetailUseCase,
    private val getStreamingVideoUrlUseCase: GetStreamingVideoUrlUseCase
) : ViewModel(){

    private  val _uiState = MutableStateFlow(DetectionDetailUiState())
    val uiState: StateFlow<DetectionDetailUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy()
    }

    fun loadDetectDetailItem(violationId: String) {
        viewModelScope.launch {
            getViolationDetailUseCase(violationId)
                .onSuccess { detail ->
                     _uiState.value = _uiState.value.copy(
                        reportDetailItem = _uiState.value.reportDetailItem.copy(
                            violationType = detail.type.toViolationType(),
                            title = "${detail.locationText}에서 ${detail.type}",
                            location = detail.locationText,
                            plateNumber = detail.plate,
                            occurredAt = Instant.parse(detail.occurredAt).toEpochMilli(),
                            createdAt = Instant.parse(detail.createdAt).toEpochMilli(),
                            objectKey = detail.objectKey,
                            videoId = detail.videoId
                        ),
                    )
                    getStreamingUrl(detail.objectKey)
                }
                .onFailure { e ->
                    _uiState.update { s -> s.copy(isLoading = false) }
                }
        }
    }

    fun getLocationFromAddress(address: String) {
        if (address.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMapLoading = true,)

            try {
                val location = getLocationFromAddressUseCase(address)
                Log.d("TAG", "getLocationFromAddress: ${location}")
                if (location != null) {
                    _uiState.value = _uiState.value.copy(
                        mapLocation = location,
                        isMapLoading = false,
                        mapError = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isMapLoading = false,
                        mapError = "주소를 찾을 수 없습니다: $address"
                    )
                }
            } catch (e: Exception) {
                Log.e("TAG", "getLocationFromAddress error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isMapLoading = false,
                    mapError = e.message ?: "위치 정보를 불러올 수 없습니다"
                )
            }
        }
    }

    fun getStreamingUrl(objectKey: String){
        viewModelScope.launch {
            val url = getStreamingVideoUrlUseCase(objectKey)
            _uiState.value = _uiState.value.copy(streamingUrl = url.url)
        }
    }

    fun navigateBack(){
        viewModelScope.launch {
            navigator.navigateBack()
        }
    }
    private fun String.toViolationType(): ViolationType = when (this.uppercase()) {
        "역주행" -> ViolationType.WRONG_WAY
        "신호위반" -> ViolationType.SIGNAL
        "차선침범" -> ViolationType.LANE
        "무번호판" -> ViolationType.NO_PLATE
        "헬멧 미착용" -> ViolationType.NO_HELMET
        "OTHERS" -> ViolationType.OTHERS
        else -> ViolationType.OTHERS
    }
}