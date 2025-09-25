package com.sos.chakhaeng.presentation.ui.screen.allbadges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.*
import com.sos.chakhaeng.presentation.ui.components.allbadges.AllBadgeItem
import com.sos.chakhaeng.presentation.ui.components.allbadges.BadgeAcquisitionDialog
import com.sos.chakhaeng.presentation.ui.components.allbadges.BadgeProgressSection
import com.sos.chakhaeng.presentation.ui.components.allbadges.EmptyContent
import com.sos.chakhaeng.presentation.ui.components.allbadges.ErrorContent
import com.sos.chakhaeng.presentation.ui.components.allbadges.LoadingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllBadgesScreen(
    uiState: AllBadgesUiState,
    viewModel: AllBadgesViewModel,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "전체 배지",
                    style = chakhaengTypography().titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "뒤로가기"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundLight
            )
        )

        // 진행률 표시 섹션
        if (uiState.hasData) {
            BadgeProgressSection(
                unlockedCount = uiState.unlockedBadgesCount,
                totalCount = uiState.totalBadgesCount,
                progressPercentage = uiState.progressPercentage
            )
        }

        // Content
        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error,
                    onRetry = {
                        viewModel.clearError()
                        viewModel.refreshBadges()
                    }
                )
            }

            uiState.hasData -> {
                val sortedBadge = uiState.badges.sortedBy {
                    it.isUnlocked == false
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedBadge) { badge ->
                        AllBadgeItem(
                            badge = badge,
                            onBadgeClick = { clickedBadge ->
                                viewModel.showBadgeDialog(clickedBadge)
                            }
                        )
                    }
                }
            }

            else -> {
                EmptyContent()
            }
        }
    }

    // 배지 획득 방법 다이얼로그
    BadgeAcquisitionDialog(
        badge = uiState.selectedBadge,
        isVisible = uiState.isBadgeDialogVisible,
        onDismiss = { viewModel.hideBadgeDialog() }
    )
}






