package com.sos.chakhaeng.core.datastore

import com.sos.chakhaeng.core.domain.model.TokenBundle
import com.sos.chakhaeng.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val tokenFlow: Flow<TokenBundle?>
    val userFlow: Flow<User?>
    suspend fun save(token: TokenBundle, user: User)
    suspend fun clear()
    suspend fun getAccessTokenOrNull(): String?
    suspend fun snapshot(): Pair<TokenBundle?, User?>
}