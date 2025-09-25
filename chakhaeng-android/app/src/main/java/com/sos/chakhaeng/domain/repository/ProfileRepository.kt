package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.profile.UserProfile

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
}