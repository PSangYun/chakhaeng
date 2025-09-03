package com.sos.chakhaeng.core.datastore.di

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.sos.chakhaeng.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager = CredentialManager.create(context)
) {

    companion object {
        private const val TAG = "GoogleAuth"
    }

    private fun buildGoogleIdOption() = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .build()

    private fun buildGetCredentialRequest() = GetCredentialRequest.Builder()
        .addCredentialOption(buildGoogleIdOption())
        .build()

    /**
     * Google One‐Tap 로그인 시도 후 ID 토큰을 반환합니다.
     * 실패 시 null.
     */
    suspend fun signInWithGoogle(activity: Activity): String? {
        return try {
            Log.d(TAG, "WEB CLIENT ID = ${BuildConfig.GOOGLE_CLIENT_ID}") // 진단
            val request = buildGetCredentialRequest()

            val cm = CredentialManager.create(activity) // ✅ Activity 기준으로 생성
            val result = cm.getCredential(activity, request)

            val googleCred = GoogleIdTokenCredential.createFrom(result.credential.data)
            googleCred.idToken
        } catch (e: Exception) {
            when (e) {
                is androidx.credentials.exceptions.GetCredentialException -> {
                    Log.e(TAG, "getCredential failed: ${e::class.simpleName} - ${e.message}", e)
                }
                else -> Log.e(TAG, "signInWithGoogle failed: ${e.message}", e)
            }
            null
        }
    }
    suspend fun googleLogout() {
        runCatching {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
        }
            .onSuccess { Log.d(TAG, "googleLogout: cleared successfully") }
            .onFailure { Log.e(TAG, "googleLogout failed: ${it.message}") }
    }
}