// core/session/SessionManager.kt
package com.sos.chakhaeng.core.session

import com.sos.chakhaeng.core.data.model.auth.SignInData
import com.sos.chakhaeng.core.data.mapper.toDomain
import com.sos.chakhaeng.core.datastore.TokenStore
import com.sos.chakhaeng.core.datastore.di.GoogleAuthManager // 고정 파일 (패키지 경로는 프로젝트에 맞춰 수정)
import com.sos.chakhaeng.core.domain.model.TokenBundle
import com.sos.chakhaeng.core.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

class SessionManager @Inject constructor(
    private val tokenStore: TokenStore,
    private val googleAuthManager: GoogleAuthManager
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // In-memory 캐시(인터셉터에서 즉시 접근)
    @Volatile private var cachedAccessToken: String? = null
    @Volatile private var accessTokenExpiresAt: Long = 0L

    init {
        scope.launch {
            combine(tokenStore.tokenFlow, tokenStore.userFlow) { t, u -> t to u }
                .collect { (t, u) ->
                    cachedAccessToken = t?.accessToken
                    accessTokenExpiresAt = t?.accessTokenExpiresAt ?: 0L
                    _authState.value = if (t != null && u != null && !isAccessExpired(System.currentTimeMillis())) {
                        AuthState.Authenticated(u)
                    } else {
                        AuthState.Unauthenticated
                    }
                }
        }
    }

    private fun isAccessExpired(now: Long): Boolean = now >= accessTokenExpiresAt

    suspend fun onLogin(signInData: SignInData, nowMs: Long = System.currentTimeMillis()) {
        val skew = 60_000L // 60s 버퍼
        val accessAt = nowMs + max(0, signInData.accessTokenExpiresIn * 1000L) - skew
        val refreshAt = signInData.refreshTokenExpiresIn?.let { nowMs + max(0, it * 1000L) - skew }

        val user: User = signInData.user.toDomain()
        val bundle = TokenBundle(
            accessToken = signInData.accessToken,
            accessTokenExpiresAt = accessAt,
            refreshToken = signInData.refreshToken,
            refreshTokenExpiresAt = refreshAt
        )
        tokenStore.save(bundle, user)
    }

    suspend fun logout() {
        tokenStore.clear()
        // 구글 계정 세션도 정리 (고정 파일 기능 활용)
        runCatching { googleAuthManager.googleLogout() }
    }

    /**
     * 현재(리프레시 없이) 만료되지 않은 액세스 토큰을 반환.
     * 만료 시 null 을 반환하고, 상위에서 재로그인을 유도.
     */
    fun getFreshAccessTokenOrNull(): String? {
        val now = System.currentTimeMillis()
        return if (!isAccessExpired(now)) cachedAccessToken else null
    }
}
