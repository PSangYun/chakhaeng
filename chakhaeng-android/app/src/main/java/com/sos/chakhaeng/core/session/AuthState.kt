// core/session/AuthState.kt
package com.sos.chakhaeng.core.session

import com.sos.chakhaeng.core.domain.model.User


sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User?) : AuthState
}
