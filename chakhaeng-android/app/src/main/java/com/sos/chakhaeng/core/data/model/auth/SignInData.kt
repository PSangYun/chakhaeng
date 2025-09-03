package com.sos.chakhaeng.core.data.model.auth

import com.google.gson.annotations.SerializedName
import com.sos.chakhaeng.core.data.model.response.UserDto

data class SignInData(
    @SerializedName("access") val accessToken: String,
    @SerializedName("accessExpiresIn") val accessTokenExpiresIn: Long,
    @SerializedName("refresh") val refreshToken: String?,
    @SerializedName("refreshExpiresIn") val refreshTokenExpiresIn: Long?,
    val firstLogin: Boolean
)