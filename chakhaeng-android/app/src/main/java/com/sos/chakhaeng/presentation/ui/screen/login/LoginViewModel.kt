package com.sos.chakhaeng.presentation.ui.screen.login

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.core.domain.usecase.auth.GoogleLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val googleAuthManager: GoogleAuthManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()


    fun login() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val idToken = googleAuthManager.signInWithGoogle()
            if (idToken.isNullOrBlank()) {
                _uiState.value = LoginUiState.Error("Google 인증이 취소되었거나 계정이 없습니다.")
                return@launch
            }
            Log.d("test1234","성공")
            val result = googleLoginUseCase(idToken)
            result
                .onSuccess {
                    user ->
                    _uiState.value = LoginUiState.Success(user)
                    Log.d("test1234","성공1")
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState.Error(e.message ?: "로그인 실패")
                    Log.d("test1234", e.toString())
                }
        }
    }
}