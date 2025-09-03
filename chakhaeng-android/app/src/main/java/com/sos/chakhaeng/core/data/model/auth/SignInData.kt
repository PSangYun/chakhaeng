package com.sos.chakhaeng.core.data.model.auth

import com.sos.chakhaeng.core.data.model.response.UserDto

data class SignInData(
    val accessToken: String,
    val accessTokenExpiresIn: Long, // seconds
    val refreshToken: String? = null,
    val refreshTokenExpiresIn: Long? = null,
    val user: UserDto
)