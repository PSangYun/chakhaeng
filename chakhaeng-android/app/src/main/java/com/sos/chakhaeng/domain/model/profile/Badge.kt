package com.sos.chakhaeng.domain.model.profile

import com.sos.chakhaeng.R

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int? = R.drawable.badge_safety,
    val isUnlocked: Boolean,
)