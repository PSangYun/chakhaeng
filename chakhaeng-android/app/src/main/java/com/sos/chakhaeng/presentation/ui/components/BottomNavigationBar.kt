package com.sos.chakhaeng.presentation.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sos.chakhaeng.presentation.navigation.bottomNavItems
import com.sos.chakhaeng.presentation.theme.NEUTRAL400
import com.sos.chakhaeng.presentation.theme.primaryLight

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        contentColor = NEUTRAL400,
        modifier = Modifier
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // 같은 화면을 다시 선택했을 때 중복 생성 방지
                        launchSingleTop = true
                        // 백스택 정리
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // 상태 복원
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryLight,
                    selectedTextColor = primaryLight,
                    unselectedIconColor = NEUTRAL400,
                    unselectedTextColor = NEUTRAL400,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}