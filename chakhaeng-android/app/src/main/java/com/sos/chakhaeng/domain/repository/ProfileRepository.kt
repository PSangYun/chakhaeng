package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.profile.Badge
import com.sos.chakhaeng.domain.model.profile.UserProfile
import com.sos.chakhaeng.domain.model.profile.Mission

interface ProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>

    suspend fun getUserBadge(): Result<List<Badge>>

    suspend fun getRecentCompletedMissions(): Result<List<Mission>>

    suspend fun getAllMissions(): Result<List<Mission>>
}