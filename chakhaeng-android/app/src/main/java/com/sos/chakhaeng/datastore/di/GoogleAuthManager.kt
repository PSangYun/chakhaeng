package com.sos.chakhaeng.datastore.di

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
        .setFilterByAuthorizedAccounts(true)//first pass true
        .setFilterByAuthorizedAccounts(false)//then false
        .build()

    private fun buildGetCredentialRequest() = GetCredentialRequest.Builder()
        .addCredentialOption(buildGoogleIdOption())
        .build()

    /**
     * Google One‐Tap 로그인 시도 후 ID 토큰을 반환합니다.
     * 실패 시 null.
     */

    suspend fun signInWithGoogle(): String? = runCatching {
        credentialManager
            .getCredential(
                request = buildGetCredentialRequest(),
                context = context
            )
            .credential
            .data
            .let(GoogleIdTokenCredential::createFrom)
            .idToken
    }
        .onFailure { e ->
            Log.e(TAG, "signInWithGoogle failed", e)
        }
        .getOrNull()
}