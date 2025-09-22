package com.sos.chakhaeng.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import com.sos.chakhaeng.core.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigator: Navigator,
) : ViewModel() {
    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(route = BottomTabRoute.Home)
    }

    fun navigateDetect() = viewModelScope.launch {
        navigator.navigate(
            route = BottomTabRoute.Detect,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateReport() = viewModelScope.launch {
        navigator.navigate(
            route = BottomTabRoute.Report,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateStats() = viewModelScope.launch {
        navigator.navigate(
            route = BottomTabRoute.Statistics,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateProfile() = viewModelScope.launch {
        navigator.navigate(
            route = BottomTabRoute.Profile,
            saveState = true,
            launchSingleTop = true,
        )
    }
}