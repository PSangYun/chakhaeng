package com.sos.chakhaeng.presentation.ui.screen.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.usecase.map.GetLocationFromAddressUseCase
import com.sos.chakhaeng.domain.usecase.report.GetReportDetailItemUseCase
import com.sos.chakhaeng.domain.usecase.video.GetStreamingVideoUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getLocationFromAddressUseCase: GetLocationFromAddressUseCase,
    private val getReportDetailItemUseCase: GetReportDetailItemUseCase,
    private val getStreamingVideoUrlUseCase: GetStreamingVideoUrlUseCase
) : ViewModel(){

    private  val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy()
    }

    fun loadReportDetailItem(reportId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading =  true,)
            getReportDetailItemUseCase(reportId)
                .onSuccess { reportDetailItem ->
                    _uiState.value = _uiState.value.copy(
                        reportDetailItem = reportDetailItem,
                        isLoading = false
                    )
                    getStreamingUrl(reportDetailItem.objectKey)
                    Log.d("TAG", "loadReportDetailItem: ${reportDetailItem}")
                }
                .onFailure { error ->
                    Log.e("TAG", "loadReportDetailItem error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
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
}