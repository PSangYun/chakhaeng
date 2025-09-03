package com.sos.chakhaeng.core.domain.model


data class TokenBundle(
    val accessToken: String,
    val accessTokenExpiresAt: Long,   // epoch millis
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: Long? = null
)