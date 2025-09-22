package com.sos.chakhaeng.presentation.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.domain.usecase.auth.GoogleLoginUseCase
import com.sos.chakhaeng.domain.usecase.auth.SendFcmTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val navigator: Navigator,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val sendFcmTokenUseCase: SendFcmTokenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Home)
    }
    fun googleLogin(idToken: String?) {
        viewModelScope.launch {
            if (idToken.isNullOrBlank()) {
                _uiState.value = LoginUiState.Error("Google 인증이 취소되었거나 계정이 없습니다.")
                return@launch
            }
            _uiState.value = LoginUiState.Loading
            val result = googleLoginUseCase(idToken)
            result
                .onSuccess {
                    user ->
                    _uiState.value = LoginUiState.Success
                    navigateHome()
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState.Error(e.message ?: "로그인 실패")
                }
        }
    }
    fun sendFcmToken(){
        viewModelScope.launch {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            sendFcmTokenUseCase(fcmToken)
        }
    }
    fun consumeSuccess() {
        if (_uiState.value is LoginUiState.Success) {
            _uiState.value = LoginUiState.Idle

        }
    }
}