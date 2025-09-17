package com.sos.chakhaeng.presentation.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import com.sos.chakhaeng.core.navigation.Navigator
import com.sos.chakhaeng.core.session.AuthState
import com.sos.chakhaeng.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val navigator: Navigator,
    sessionManager: SessionManager
): ViewModel() {
    val authState: StateFlow<AuthState> = sessionManager.authState

    lateinit var controller: LifecycleCameraController
        private set

    fun init(activity: ComponentActivity) {
        if (::controller.isInitialized) return
        controller = LifecycleCameraController(app).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            // 필요에 따라 활성화할 UseCase 설정
            setEnabledUseCases(
                CameraController.VIDEO_CAPTURE or CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE
            )
            bindToLifecycle(activity)
        }
    }
    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Home)
    }
    fun navigateDetect() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Detect)
    }
    fun navigateReport() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Report)
    }
    fun navigateStats() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Statistics)
    }
    fun navigateProfile() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Profile)
    }
}