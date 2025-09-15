package com.sos.chakhaeng.presentation.ui.screen.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.domain.model.location.Location
import com.sos.chakhaeng.domain.usecase.location.GetLocationFromAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getLocationFromAddressUseCase: GetLocationFromAddressUseCase
) : ViewModel(){

    private  val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getLocationFromAddress(address: String) {
        if (address.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMapLoading = true)

            try {
                val location = getLocationFromAddressUseCase(address)
                _uiState.value = _uiState.value.copy(
                    mapLocation = location ?: Location.DEFAULT,
                    isMapLoading = false,
                    mapError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMapLoading = false,
                    mapError = e.message ?: "위치 정보를 불러올 수 없습니다"
                )
            }
        }
    }
}