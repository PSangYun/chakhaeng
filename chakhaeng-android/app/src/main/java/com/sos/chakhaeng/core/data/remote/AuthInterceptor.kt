// core/data/remote/AuthInterceptor.kt
package com.sos.chakhaeng.core.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import com.sos.chakhaeng.core.session.SessionManager

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        // 로그인 엔드포인트는 제외
        val path = req.url.encodedPath
        if (path.contains("/auth/google")) {
            return chain.proceed(req)
        }

        val token = sessionManager.getFreshAccessTokenOrNull()
        val newReq = if (token != null) {
            req.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            req // 토큰 없으면 그대로 (서버가 401 응답할 수 있음)
        }
        return chain.proceed(newReq)
    }
}
