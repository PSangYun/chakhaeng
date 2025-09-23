// presentation/ui/screen/profile/ProfileScreen.kt
package com.sos.chakhaeng.presentation.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.sos.chakhaeng.presentation.theme.BackgroundGray
import com.sos.chakhaeng.presentation.ui.components.profile.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        when {
            uiState.isLoading -> {
                LoadingSection(
                    message = "프로필 정보를 불러오고 있습니다..."
                )
            }

            uiState.error != null -> {
                ErrorSection(
                    error = uiState.error,
                    onRetry = { viewModel.refreshProfile() }
                )
            }

            uiState.hasData -> {
                LogoutSection(
                    onLogoutClick = { viewModel.showLogoutDialog() }
                )

                ProfileContent(
                    uiState = uiState
                )
            }
        }
    }

    LogoutConfirmDialog(
        isVisible = uiState.isLogoutDialogVisible,
        onConfirm = {
            viewModel.hideLogoutDialog()
            viewModel.logout()
        },
        onDismiss = {
            viewModel.hideLogoutDialog()
        }
    )
}