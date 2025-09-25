package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.network.dto.response.profile.UserProfileDTO
import com.sos.chakhaeng.domain.model.profile.UserProfile

object ProfileDataMapper {
    fun UserProfileDTO.toEntity(): UserProfile = UserProfile(
        name = name,
        title = title,
        profileImageUrl = profileImageUrl
    )
}