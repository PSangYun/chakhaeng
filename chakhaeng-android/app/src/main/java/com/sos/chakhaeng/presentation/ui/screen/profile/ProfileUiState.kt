package com.sos.chakhaeng.presentation.ui.screen.profile

import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.model.profile.Mission
import com.sos.chakhaeng.domain.model.profile.UserProfile


data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val userProfile: UserProfile? = null,
    val badges: List<Badge> = emptyList(),
    val missions: List<Mission> = emptyList(),
    val isLogoutDialogVisible: Boolean = false
) {
    companion object{
        fun loading() = ProfileUiState(isLoading = true)
        fun error(message: String) = ProfileUiState(error = message)
        fun success(
            userProfile: UserProfile,
            badges: List<Badge>,
            missions: List<Mission>
        ) = ProfileUiState(
            isLoading = false,
            userProfile = userProfile,
            badges = badges,
            missions = missions
        )
    }

    val hasData: Boolean
        get() = userProfile != null && !isLoading && error == null
}
