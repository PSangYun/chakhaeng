package com.sos.chakhaeng.presentation.ui.screen.login

import com.sos.chakhaeng.core.domain.model.User

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}