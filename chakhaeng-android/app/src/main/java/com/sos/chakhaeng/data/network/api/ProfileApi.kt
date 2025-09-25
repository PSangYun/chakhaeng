package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.response.profile.BadgeDTO
import com.sos.chakhaeng.data.network.dto.response.profile.UserProfileDTO
import retrofit2.http.GET

interface ProfileApi {
    @GET("profile")
    suspend fun getUserProfile(): ApiResponse<UserProfileDTO>

    @GET("profile/badge")
    suspend fun getUserBadge(): ApiResponse<List<BadgeDTO>>

}