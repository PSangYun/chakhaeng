package com.sos.chakhaeng.domain.model.profile

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isCompleted: Boolean,
)
