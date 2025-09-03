package com.sos.chakhaeng.presentation.ui.screen.login

import com.sos.chakhaeng.core.domain.model.User

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val user: User) : LoginUiState
    data class Error(val message: String) : LoginUiState
}