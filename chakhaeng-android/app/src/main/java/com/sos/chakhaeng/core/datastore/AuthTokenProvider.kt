// core/data/auth/token/AuthTokenProvider.kt
package com.sos.chakhaeng.core.data.auth.token

import com.sos.chakhaeng.core.data.auth.datastore.AuthDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor(
    authDataStore: AuthDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tokenState = MutableStateFlow<String?>(null)

    init {
        scope.launch {
            authDataStore.accessTokenFlow.collectLatest { tokenState.value = it }
        }
    }

    fun getAccessToken(): String? = tokenState.value
    fun clearInMemory() { tokenState.value = null }
}
