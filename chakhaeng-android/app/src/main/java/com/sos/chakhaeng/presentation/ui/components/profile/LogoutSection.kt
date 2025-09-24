package com.sos.chakhaeng.presentation.ui.components.profile

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.errorLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutSection(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(
            text = "프로필",
            style = chakhaengTypography().titleSmall,
            fontWeight = FontWeight.SemiBold
        ) },
        actions = {
            IconButton(onClick = {onLogoutClick()}) {
                Icon(Icons.Default.Logout, contentDescription = "뒤로")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        windowInsets = WindowInsets(0)
    )
}

@Composable
fun LogoutConfirmDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = errorLight,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "정말 로그아웃 하시겠습니까?",
                    style = chakhaengTypography().titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorLight
                    )
                ) {
                    Text(
                        text = "로그아웃",
                        color = Color.White,
                        style = chakhaengTypography().labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "취소",
                        color = Color(0xFF6C757D),
                        style = chakhaengTypography().labelMedium
                    )
                }
            },
            containerColor = Color.White,
            titleContentColor = Color(0xFF212529),
            textContentColor = Color(0xFF6C757D)
        )
    }
}