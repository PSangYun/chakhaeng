package com.sos.chakhaeng.presentation.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.domain.model.User
import com.sos.chakhaeng.domain.repository.AuthRepository
import com.sos.chakhaeng.domain.usecase.auth.GoogleLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val navigator: Navigator,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Home)
    }
    fun googleLogin(idToken: String?, user: User? = null) {
        viewModelScope.launch {
            if (idToken.isNullOrBlank()) {
                _uiState.value = LoginUiState.Error("Google 인증이 취소되었거나 계정이 없습니다.")
                return@launch
            }
            _uiState.value = LoginUiState.Loading

            // Google 사용자 정보를 AuthRepository에 설정
            authRepository.setGoogleUser(user)

            val result = googleLoginUseCase(idToken)
            result
                .onSuccess {
                    loginResult ->
                    _uiState.value = LoginUiState.Success
                    navigateHome()
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState.Error(e.message ?: "로그인 실패")
                }
        }
    }

    fun consumeSuccess() {
        if (_uiState.value is LoginUiState.Success) {
            _uiState.value = LoginUiState.Idle

        }
    }
}