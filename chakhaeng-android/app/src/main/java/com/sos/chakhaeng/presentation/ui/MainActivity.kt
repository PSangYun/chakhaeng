package com.sos.chakhaeng.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sos.chakhaeng.presentation.ui.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.ui.navigation.ChakhaengNavigation
import com.sos.chakhaeng.presentation.ui.components.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.padding

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChakHaengTheme {
                ChakhaengApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChakhaengApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        ChakhaengNavigation(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}