package com.sos.chakhaeng.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import com.sos.chakhaeng.core.navigation.LaunchedNavigator
import com.sos.chakhaeng.core.navigation.Route
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.core.session.SessionManager
import com.sos.chakhaeng.presentation.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.ui.components.BottomNavigationBar
 import com.sos.chakhaeng.presentation.ui.screen.allbadges.AllBadgesRoute
import com.sos.chakhaeng.presentation.ui.screen.detection.DetectionScreen
import com.sos.chakhaeng.presentation.ui.screen.home.HomeRoute
import com.sos.chakhaeng.presentation.ui.screen.login.LoginScreen
import com.sos.chakhaeng.presentation.ui.screen.profile.ProfileRoute
import com.sos.chakhaeng.presentation.ui.screen.report.ReportRoute
import com.sos.chakhaeng.presentation.ui.screen.reportdetail.ReportDetailRoute
import com.sos.chakhaeng.presentation.ui.screen.statistics.StatisticsRoute
import com.sos.chakhaeng.presentation.ui.screen.violationDetail.ViolationDetailScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var googleAuthManager: GoogleAuthManager

    @Inject
    lateinit var sessionManager: SessionManager

    private var isLoading by mutableStateOf(true)

    private fun performInitialization() {
        isLoading = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        performInitialization()
        splashScreen.setKeepOnScreenCondition {
            isLoading
        }
        enableEdgeToEdge()
        setContent {
            ChakHaengTheme {
                var startDestination: NavKey? by remember { mutableStateOf(null) }

                LaunchedEffect(Unit) {
                    startDestination = if (sessionManager.autoLogin()) {
                        BottomTabRoute.Home
                    } else {
                        Route.Login
                    }
                }
                startDestination?.let { destination ->
                    val navBackStack = rememberNavBackStack(destination)

                    LaunchedNavigator(navBackStack)
                    if (!isLoading) {
                        ChakhaengApp(
                            navBackStack,
                            googleAuthManager = googleAuthManager,
                            onTabSelected = {
                                when (it.route) {
                                    BottomTabRoute.Home -> viewModel.navigateHome()
                                    BottomTabRoute.Profile -> viewModel.navigateProfile()
                                    BottomTabRoute.Detect -> viewModel.navigateDetect()
                                    BottomTabRoute.Report -> viewModel.navigateReport()
                                    BottomTabRoute.Statistics -> viewModel.navigateStats()
                                }
                            }
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChakhaengApp(
    navBackStack: NavBackStack,
    googleAuthManager: GoogleAuthManager,
    onTabSelected: (MainTab) -> Unit
) {
    val currentRoute = navBackStack.lastOrNull()
    val currentTab = when (currentRoute) {
        is BottomTabRoute.Home -> MainTab.HOME
        is BottomTabRoute.Detect -> MainTab.Detects
        is BottomTabRoute.Report -> MainTab.Reports
        is BottomTabRoute.Statistics -> MainTab.Statistics
        is BottomTabRoute.Profile -> MainTab.PROFILE
        else -> MainTab.HOME
    }

    Scaffold(
        bottomBar = {
            if (currentRoute !is Route) {
                BottomNavigationBar(
                    tabs = MainTab.entries.toImmutableList(),
                    currentTab = currentTab,
                    onTabSelected = onTabSelected,
                )
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier
                .background(Color.White),
            entryDecorators = listOf(
                // Add the default decorators for managing scenes and saving state
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            backStack = navBackStack,
            transitionSpec = {
                ContentTransform(
                    fadeIn(animationSpec = tween(0)),
                    fadeOut(animationSpec = tween(0)),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    fadeIn(animationSpec = tween(0)),
                    fadeOut(animationSpec = tween(0)),
                )
            },
            onBack = { navBackStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is BottomTabRoute.Home -> NavEntry(key) {
                        HomeRoute(
                            padding = paddingValues
                        )
                    }

                    is BottomTabRoute.Detect -> NavEntry(key) {
                        DetectionScreen(
                            paddingValues = paddingValues
                        )
                    }

                    is BottomTabRoute.Report -> NavEntry(key) {
                        ReportRoute(
                            padding = paddingValues
                        )
                    }

                    is BottomTabRoute.Statistics -> NavEntry(key) {
                        StatisticsRoute(padding = paddingValues)
                    }

                    is BottomTabRoute.Profile -> NavEntry(key) {
                        ProfileRoute(
                            padding = paddingValues,
                            navBackStack = navBackStack
                        )
                    }

                    is Route.Login -> NavEntry(key){
                        LoginScreen(googleAuthManager = googleAuthManager)
                    }

                    is Route.ReportDetail -> NavEntry(key){
                        ReportDetailRoute(
                            padding = paddingValues,
                            reportId = key.reportId,
                        )
                    }

                    is Route.ViolationDetail -> NavEntry(key){
                        ViolationDetailScreen(
                            paddingValues = paddingValues,
                            violationId = key.violationId
                        )
                    }

                    is Route.AllBadges -> NavEntry(key) {
                        AllBadgesRoute(
                            navBackStack = navBackStack
                        )
                    }

                    else -> NavEntry(key) { Unit }
                }
            },
        )
    }
}