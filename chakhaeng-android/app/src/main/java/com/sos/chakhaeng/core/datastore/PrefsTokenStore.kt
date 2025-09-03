// core/datastore/PrefsTokenStore.kt
package com.sos.chakhaeng.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.sos.chakhaeng.core.domain.model.TokenBundle
import com.sos.chakhaeng.core.domain.model.User
import kotlinx.coroutines.flow.firstOrNull

class PrefsTokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : TokenStore {

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val ACCESS_EXP = longPreferencesKey("access_token_expires_at")
        val REFRESH = stringPreferencesKey("refresh_token")
        val REFRESH_EXP = longPreferencesKey("refresh_token_expires_at")

        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PIC = stringPreferencesKey("user_picture")
    }

    override val tokenFlow: Flow<TokenBundle?> = dataStore.data.map { p ->
        val access = p[Keys.ACCESS] ?: return@map null
        val accessExp = p[Keys.ACCESS_EXP] ?: return@map null
        val refresh = p[Keys.REFRESH]
        val refreshExp = p[Keys.REFRESH_EXP]
        TokenBundle(access, accessExp, refresh, refreshExp)
    }

    override val userFlow: Flow<User?> = dataStore.data.map { p ->
        val id = p[Keys.USER_ID] ?: return@map null
        val email = p[Keys.USER_EMAIL] ?: return@map null
        val name = p[Keys.USER_NAME] ?: ""
        val pic = p[Keys.USER_PIC] ?: ""
        User(id, email, name, pic)
    }

    override suspend fun save(token: TokenBundle, user: User?) {
        dataStore.edit { p ->
            // 토큰 저장
            p[Keys.ACCESS] = token.accessToken
            p[Keys.ACCESS_EXP] = token.accessTokenExpiresAt
            token.refreshToken?.let { p[Keys.REFRESH] = it } ?: run { p.remove(Keys.REFRESH) }
            token.refreshTokenExpiresAt?.let { p[Keys.REFRESH_EXP] = it } ?: run { p.remove(Keys.REFRESH_EXP) }

            if (user != null) {
                p[Keys.USER_ID] = user.id
                p[Keys.USER_EMAIL] = user.email
                p[Keys.USER_NAME] = user.name
                p[Keys.USER_PIC] = user.pictureUrl
            } else {
                p.remove(Keys.USER_ID)
                p.remove(Keys.USER_EMAIL)
                p.remove(Keys.USER_NAME)
                p.remove(Keys.USER_PIC)
            }

        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override suspend fun getAccessTokenOrNull(): String? =
        dataStore.data.map { it[Keys.ACCESS] }.firstOrNull()

    override suspend fun snapshot(): Pair<TokenBundle?, User?> {
        val prefs = dataStore.data.firstOrNull() ?: return null to null
        val t = prefs[Keys.ACCESS]?.let { a ->
            val ae = prefs[Keys.ACCESS_EXP] ?: return@let null
            val r = prefs[Keys.REFRESH]
            val re = prefs[Keys.REFRESH_EXP]
            TokenBundle(a, ae, r, re)
        }
        val u = prefs[Keys.USER_ID]?.let { id ->
            val email = prefs[Keys.USER_EMAIL] ?: return@let null
            val name = prefs[Keys.USER_NAME] ?: ""
            val pic = prefs[Keys.USER_PIC] ?: ""
            User(id, email, name, pic)
        }
        return t to u
    }
}
