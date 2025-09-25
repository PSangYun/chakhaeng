package com.sos.chakhaeng.domain.model.profile

import com.sos.chakhaeng.R

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int? = R.drawable.badge_safety,
    val isCompleted: Boolean,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val rewardName: String = ""
) {
    val progressPercentage: Float
        get() = if (targetProgress > 0) {
            (currentProgress.toFloat() / targetProgress.toFloat() * 100f).coerceAtMost(100f)
        } else 100f
}