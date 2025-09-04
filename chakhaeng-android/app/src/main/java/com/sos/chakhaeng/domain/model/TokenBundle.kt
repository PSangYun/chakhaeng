package com.sos.chakhaeng.domain.model


data class TokenBundle(
    val accessToken: String,
    val accessTokenExpiresAt: Long,   // epoch millis
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: Long? = null
)