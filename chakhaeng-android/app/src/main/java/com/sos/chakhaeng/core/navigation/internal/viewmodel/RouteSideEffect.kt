package com.sos.chakhaeng.core.navigation.internal.viewmodel

import androidx.navigation3.runtime.NavKey

sealed interface RouteSideEffect {
    data class Navigate(
        val route: NavKey,
        val saveState: Boolean,
        val launchSingleTop: Boolean
    ) : RouteSideEffect

    data object NavigateBack : RouteSideEffect

    data class NavigateAndClearBackStack(
        val route: NavKey
    ) : RouteSideEffect
}
