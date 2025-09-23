package com.sos.chakhaeng.domain.model.profile

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean,
)