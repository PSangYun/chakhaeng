package com.sos.chakhaeng.core.session

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    val sessionManager: SessionManager
): ViewModel() {
    val authState: StateFlow<AuthState> = sessionManager.authState
}