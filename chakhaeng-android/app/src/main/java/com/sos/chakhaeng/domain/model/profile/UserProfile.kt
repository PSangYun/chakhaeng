package com.sos.chakhaeng.domain.model.profile

data class UserProfile(
    val id: String,
    val name: String,
    val title: String,
    val profileImageUrl: String? = null
)
