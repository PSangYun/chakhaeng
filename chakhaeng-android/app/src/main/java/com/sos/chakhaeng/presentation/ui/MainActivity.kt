package com.sos.chakhaeng.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sos.chakhaeng.presentation.ui.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.ui.navigation.ChakhaengNavigation
import com.sos.chakhaeng.presentation.ui.components.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sos.chakhaeng.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.session.AppEntryViewModel
import com.sos.chakhaeng.session.AuthState
import com.sos.chakhaeng.presentation.ui.navigation.Routes
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var googleAuthManager: GoogleAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChakHaengTheme {
                ChakhaengApp(googleAuthManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChakhaengApp(googleAuthManager: GoogleAuthManager) {
    val navController = rememberNavController()

    val vm: AppEntryViewModel = hiltViewModel()
    val authState by vm.authState.collectAsState()

    val startDestination = remember(authState) {
        if (authState is AuthState.Authenticated) Routes.Home.route else Routes.Login.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar by remember(currentRoute) {
        derivedStateOf {
            currentRoute != Routes.Login.route
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        ChakhaengNavigation(
            navController = navController,
            modifier = Modifier,
            googleAuthManager = googleAuthManager,
            startDestination = startDestination,
            paddingValues = paddingValues
        )

        LaunchedEffect(authState) {
            when(authState) {
                is AuthState.Authenticated -> {
                    if (navController.currentDestination?.route != Routes.Home.route) {
                        navController.navigate(Routes.Home.route) {
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
            }
        }
    }
}