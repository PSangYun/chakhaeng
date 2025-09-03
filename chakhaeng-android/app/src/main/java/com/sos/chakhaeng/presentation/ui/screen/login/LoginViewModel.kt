package com.sos.chakhaeng.presentation.ui.screen.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.BuildConfig
import com.sos.chakhaeng.core.data.service.AuthService
import com.sos.chakhaeng.core.domain.usecase.auth.GoogleLoginUseCase
import com.sos.chakhaeng.presentation.ui.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginUseCase: GoogleLoginUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val googleClientId = BuildConfig.GOOGLE_CLIENT_ID

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()


    fun login(getIdToken: suspend () -> String?) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val idToken = getIdToken()
            if (idToken.isNullOrBlank()) {
                _uiState.value = LoginUiState.Error("Google 인증이 취소되었거나 계정이 없습니다.")
                return@launch
            }
            val result = googleLoginUseCase(idToken)
            result
                .onSuccess { user -> _uiState.value = LoginUiState.Success(user) }
                .onFailure { e -> _uiState.value = LoginUiState.Error(e.message ?: "로그인 실패") }
        }
    }
}