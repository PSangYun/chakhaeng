package com.sos.chakhaeng.presentation.ui.screen.allbadges

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.R
import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.usecase.profile.GetUserBadgeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllBadgesViewModel @Inject constructor(
    private val getUserBadgeUseCase: GetUserBadgeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllBadgesUiState())
    val uiState: StateFlow<AllBadgesUiState> = _uiState.asStateFlow()

    init {
        loadAllBadges()
    }

    fun showBadgeDialog(badge: Badge) {
        _uiState.value = _uiState.value.copy(
            selectedBadge = badge,
            isBadgeDialogVisible = true
        )
    }

    fun hideBadgeDialog() {
        _uiState.value = _uiState.value.copy(
            selectedBadge = null,
            isBadgeDialogVisible = false
        )
    }

    fun refreshBadges() {
        loadAllBadges()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun loadAllBadges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getUserBadgeUseCase()
            .onSuccess { badges ->
                _uiState.value = _uiState.value.copy(
                    badges = badges,
                    isLoading = false
                )
                Log.d("TAG", "loadAllBadges: ${badges}")
            }


            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    isLoading = false
                )
            }
        }
    }
}