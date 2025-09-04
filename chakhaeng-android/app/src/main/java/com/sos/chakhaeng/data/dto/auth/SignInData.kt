package com.sos.chakhaeng.data.dto.auth

import com.google.gson.annotations.SerializedName

data class SignInData(
    @SerializedName("access") val accessToken: String,
    @SerializedName("accessExpiresIn") val accessTokenExpiresIn: Long,
    @SerializedName("refresh") val refreshToken: String?,
    @SerializedName("refreshExpiresIn") val refreshTokenExpiresIn: Long?,
    val firstLogin: Boolean
)