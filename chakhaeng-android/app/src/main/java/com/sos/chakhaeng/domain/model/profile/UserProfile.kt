package com.sos.chakhaeng.domain.model.profile

data class UserProfile(
    val name: String,
    val title: String,
    val profileImageUrl: String? = null
)
