// core/data/remote/AuthInterceptor.kt
package com.sos.chakhaeng.data.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import com.sos.chakhaeng.core.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Request

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        Log.d("AuthInt", "→ ${req.method} ${req.url.encodedPath}")

        if (shouldSkipAuth(req)) {
            return chain.proceed(req)
        }

        runBlocking {
            sessionManager.refreshIfNeeded()
        }

        val token = sessionManager.getFreshAccessTokenOrNull()
        Log.d("AuthInt", "token? ${!token.isNullOrBlank()} / ${token?.take(12)}…")
        val newReq = if (token != null) {
            req.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            req // 토큰 없으면 그대로 (서버가 401 응답할 수 있음)
        }
        return chain.proceed(newReq)
    }

    private fun shouldSkipAuth(req: Request): Boolean {
        val path = req.url.encodedPath
        // 필요시 public 경로도 여기에 추가
        return path.startsWith("/auth/")
                || path.contains("/auth/google")
                || path.contains("/auth/refresh")
                || path.contains("/auth/logout")
    }
}
