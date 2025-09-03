// core/data/auth/datastore/AuthDataStore.kt
package com.sos.chakhaeng.core.data.auth.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val DS_NAME = "auth_prefs"
private val Context.authDataStore by preferencesDataStore(DS_NAME)

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_AT = stringPreferencesKey("access_token")
    private val KEY_AT_EXP = longPreferencesKey("access_exp_at")       // epoch millis
    private val KEY_RT = stringPreferencesKey("refresh_token")
    private val KEY_RT_EXP = longPreferencesKey("refresh_exp_at")

    private val KEY_UID = stringPreferencesKey("user_id")
    private val KEY_EMAIL = stringPreferencesKey("user_email")
    private val KEY_NAME = stringPreferencesKey("user_name")
    private val KEY_PIC = stringPreferencesKey("user_pic")

    val accessTokenFlow: Flow<String?> = context.authDataStore.data.map { it[KEY_AT] }
    val accessExpAtFlow: Flow<Long?> = context.authDataStore.data.map { it[KEY_AT_EXP] }

    suspend fun saveAll(
        accessToken: String,
        accessExpAt: Long,
        refreshToken: String?,
        refreshExpAt: Long?,
        user: UserPrefs
    ) {
        context.authDataStore.edit {
            it[KEY_AT] = accessToken
            it[KEY_AT_EXP] = accessExpAt
            if (refreshToken != null) it[KEY_RT] = refreshToken
            if (refreshExpAt != null) it[KEY_RT_EXP] = refreshExpAt
            it[KEY_UID] = user.id
            it[KEY_EMAIL] = user.email
            it[KEY_NAME] = user.name
            it[KEY_PIC] = user.pictureUrl
        }
    }

    suspend fun clear() = context.authDataStore.edit { it.clear() }

    data class UserPrefs(val id: String, val email: String, val name: String, val pictureUrl: String)
}
