package com.sos.chakhaeng.presentation.ui.screen.allbadges

import com.sos.chakhaeng.domain.model.profile.Badge

data class AllBadgesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val badges: List<Badge> = emptyList(),
    val selectedBadge: Badge? = null,
    val isBadgeDialogVisible: Boolean = false,
) {
    companion object {
        fun loading() = AllBadgesUiState(isLoading = true)
        fun error(message: String) = AllBadgesUiState(
            isLoading = false,
            error = message
        )
        fun success(
            badges: List<Badge>,
        ) = AllBadgesUiState(
            badges = badges,
            isLoading = false,
        )
    }

    val hasData: Boolean
        get() = badges.isNotEmpty() && !isLoading && error == null

    val unlockedBadgesCount: Int
        get() = badges.count { it.isUnlocked }

    val totalBadgesCount: Int
        get() = badges.size

    val progressPercentage: Float
        get() = if (totalBadgesCount > 0) {
            (unlockedBadgesCount.toFloat() / totalBadgesCount.toFloat()) * 100f
        } else 0f
}