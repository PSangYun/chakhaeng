package com.sos.chakhaeng.presentation.main

import androidx.lifecycle.ViewModel
import com.sos.chakhaeng.core.session.AuthState
import com.sos.chakhaeng.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    val sessionManager: SessionManager
): ViewModel() {
    val authState: StateFlow<AuthState> = sessionManager.authState
}