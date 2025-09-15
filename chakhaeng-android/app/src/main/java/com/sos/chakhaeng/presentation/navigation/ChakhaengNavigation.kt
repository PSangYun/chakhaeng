package com.sos.chakhaeng.presentation.navigation

import StreamingScreen
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sos.chakhaeng.presentation.ui.screen.home.HomeScreen
import com.sos.chakhaeng.presentation.ui.screen.detection.DetectionScreen
import com.sos.chakhaeng.presentation.ui.screen.report.ReportScreen
import com.sos.chakhaeng.presentation.ui.screen.statistics.StatisticsScreen
import com.sos.chakhaeng.presentation.ui.screen.profile.ProfileScreen
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sos.chakhaeng.core.session.AuthState
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.presentation.main.AppEntryViewModel
import com.sos.chakhaeng.presentation.ui.screen.login.LoginScreen
import com.sos.chakhaeng.presentation.ui.screen.violationDetail.ViolationDetailScreen

@Composable
fun ChakhaengNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    googleAuthManager: GoogleAuthManager,
    startDestination: String,
    paddingValues: PaddingValues,
    authState: AuthState,
    appEntryViewModel: AppEntryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(700)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(700)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(700)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(700)
            )
        },
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                navigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                googleAuthManager = googleAuthManager
            )
        }
        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToDetection = {
                    navController.navigate(Routes.Detection.route) {
                        launchSingleTop = true
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                },
                paddingValues = paddingValues
            )
        }
        composable(Routes.Detection.route) {
            DetectionScreen(
                onCreateNewViolation = {
                    navController.navigate(Routes.ViolationDetail.route)
                },
                onViolationClick = {
                    navController.navigate(Routes.ViolationDetail.route)
                },
                paddingValues = paddingValues,
                appEntryViewModel = appEntryViewModel
            )
        }
        composable(Routes.Report.route) {
            ReportScreen(
                paddingValues = paddingValues
            )
        }
        composable(Routes.Statistics.route) {
            StatisticsScreen()
        }
        composable(Routes.Profile.route) {
            ProfileScreen(
                navigateToStreaming = {
                    navController.navigate(Routes.Streaming.route)
                }
            )
        }
        composable(
            route = Routes.ViolationDetail.route,
            arguments = listOf(navArgument("id"){type = NavType.StringType; nullable = true})) {
            ViolationDetailScreen(
                onBack = { navController.popBackStack() },
                paddingVaules = paddingValues
            )
        }
        composable(Routes.Streaming.route) {
            StreamingScreen()
        }
    }
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route != Routes.Home.route) {
                    navController.navigate(Routes.Home.route) {
                        Log.d("TAG", "ChakhaengApp: 123")
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }

            AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != Routes.Login.route) {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }

            AuthState.Loading -> Unit
        }
    }
}