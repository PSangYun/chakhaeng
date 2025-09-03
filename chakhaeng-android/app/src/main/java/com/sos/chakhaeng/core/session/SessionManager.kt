// core/session/SessionManager.kt
package com.sos.chakhaeng.core.session

import com.sos.chakhaeng.core.data.model.auth.SignInData
import com.sos.chakhaeng.core.data.model.request.RefreshTokenRequest
import com.sos.chakhaeng.core.data.service.AuthService
import com.sos.chakhaeng.core.datastore.TokenStore
import com.sos.chakhaeng.core.datastore.di.GoogleAuthManager // 고정 파일 (패키지 경로는 프로젝트에 맞춰 수정)
import com.sos.chakhaeng.core.domain.model.TokenBundle
import com.sos.chakhaeng.core.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.max

class SessionManager @Inject constructor(
    private val tokenStore: TokenStore,
    private val googleAuthManager: GoogleAuthManager,
    @Named("noauth") private val authService: AuthService,
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val refreshMutex = Mutex()
    private val skewMs = 60_000L

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

                    val now = System.currentTimeMillis()
                    val tokenValid = t != null && !isAccessExpired(now)

                    _authState.value = if (tokenValid) {
                        AuthState.Authenticated(u)
                    } else {
                        AuthState.Unauthenticated
                    }
                }
        }
    }

    private fun isAccessExpired(now: Long): Boolean = now >= accessTokenExpiresAt
    private fun isAccessExpiringSoon(now: Long): Boolean = now + 10_000L >= accessTokenExpiresAt // 10초 이내 만료면 임박

    /** 요청 직전에 호출: 만료 임박 시 선제 갱신 */
    suspend fun refreshIfNeeded(now: Long = System.currentTimeMillis()): Boolean {
        val (t, _) = tokenStore.snapshot()
        if (t == null) return false
        if (!isAccessExpiringSoon(now)) return true
        return doRefreshLocked()
    }

    /** 401에서 호출: 한 번만 강제 갱신 */
    suspend fun forceRefresh(): Boolean = doRefreshLocked()

    private suspend fun doRefreshLocked(): Boolean = refreshMutex.withLock {
        // 스냅샷 재확인 (동시성 고려)
        val (current, _) = tokenStore.snapshot()
        val refresh = current?.refreshToken ?: return false

        // 리프레시 만료 체크
        current.refreshTokenExpiresAt?.let { if (it <= System.currentTimeMillis()) {
            // 리프레시도 만료 → 완전 로그아웃
            tokenStore.clear()
            _authState.value = AuthState.Unauthenticated
            return false
        } }

        return runCatching {
            val resp = authService.refreshToken(RefreshTokenRequest(refresh)).data ?: return@runCatching false
            val now = System.currentTimeMillis()
            fun toAt(sec: Long?): Long? {
                if (sec == null) return null
                val ms = sec * 1000L
                val safe = if (ms > skewMs) ms - skewMs else 0L
                return now + safe
            }
            val newAccessAt  = toAt(resp.accessTokenExpiresIn) ?: now
            val newRefreshAt = toAt(resp.refreshTokenExpiresIn) ?: current?.refreshTokenExpiresAt
            val newAccess    = resp.accessToken
            val newRefresh   = resp.refreshToken ?: current?.refreshToken

            tokenStore.save(
                TokenBundle(
                    accessToken = newAccess,
                    accessTokenExpiresAt = newAccessAt,
                    refreshToken = newRefresh,
                    refreshTokenExpiresAt = newRefreshAt
                ),
                user = null // 프로필은 나중에 /users/me
            )
            cachedAccessToken = resp.accessToken
            accessTokenExpiresAt = newAccessAt
            true
        }.getOrElse { e ->
            // 갱신 실패시 깔끔히 끊고 재로그인 유도
            tokenStore.clear()
            _authState.value = AuthState.Unauthenticated
            false
        }
    }

    suspend fun onLogin(signInData: SignInData, nowMs: Long = System.currentTimeMillis()) {
        fun toExpiresAt(inSeconds: Long?): Long? {
            if (inSeconds == null) return null
            val totalMs = inSeconds * 1000L
            val safeMs = if (totalMs > skewMs) totalMs - skewMs else 0L
            return nowMs + safeMs
        }

        val accessAt  = toExpiresAt(signInData.accessTokenExpiresIn)
        val refreshAt = toExpiresAt(signInData.refreshTokenExpiresIn)

        val bundle = TokenBundle(
            accessToken = signInData.accessToken,
            accessTokenExpiresAt = accessAt ?: nowMs,
            refreshToken = signInData.refreshToken,
            refreshTokenExpiresAt = refreshAt
        )

        // 지금은 프로필 없음: TokenStore.save(token, user = null)
        tokenStore.save(bundle, null)
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
