package com.sos.chakhaeng.core.data.remote

import com.sos.chakhaeng.core.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManager: SessionManager
): Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {

        if (responseCount(response) >= 2) return null

        val ok = runBlocking { sessionManager.forceRefresh() }
        if (!ok) return null

        val token = sessionManager.getFreshAccessTokenOrNull() ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var r = response.priorResponse
        while (r != null) {
            count++; r = r.priorResponse
        }
        return count
    }
}