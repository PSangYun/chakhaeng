package com.sos.chakhaeng.presentation.ui.components.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.ui.screen.profile.ProfileUiState

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onAllBadgesClick: () -> Unit,
    onMissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // 사용자 정보 섹션
        item {
            uiState.userProfile?.let { userProfile ->
                UserInfoSection(
                    userProfile = userProfile
                )
            }
        }

        // 배지 정보 섹션
        item {
            BadgeInfoSection(
                badges = uiState.badges,
                onAllBadgesClick = onAllBadgesClick
            )
        }

        // 미션 정보 섹션
        item {
            MissionInfoSection(
                missions = uiState.recentCompletedMissions,
                isRecentMissionEmpty = uiState.isRecentMissionEmpty,
                onMissionClick = onMissionClick
            )
        }
    }
}