package com.sos.chakhaeng.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sos.chakhaeng.core.navigation.internal.viewmodel.NavigatorViewModel
import com.sos.chakhaeng.core.navigation.internal.viewmodel.RouteSideEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LaunchedNavigator(navBackStack: NavBackStack<NavKey>) {
    InternalLaunchedNavigator(
        navBackStack = navBackStack,
    )
}

@Composable
private fun InternalLaunchedNavigator(
    navBackStack: NavBackStack<NavKey>,
    routerViewModel: NavigatorViewModel = hiltViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(routerViewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            routerViewModel.sideEffect.collectLatest { sideEffect ->
                when (sideEffect) {
                    is RouteSideEffect.NavigateBack -> {
                        navBackStack.removeLastOrNull()
                    }

                    is RouteSideEffect.Navigate -> {
                        navBackStack.remove(sideEffect.route)
                        navBackStack.add(sideEffect.route)
                    }

                    is RouteSideEffect.NavigateAndClearBackStack -> {
                        navBackStack.clear()
                        navBackStack.add(sideEffect.route)
                    }
                }
            }
        }
    }
}
