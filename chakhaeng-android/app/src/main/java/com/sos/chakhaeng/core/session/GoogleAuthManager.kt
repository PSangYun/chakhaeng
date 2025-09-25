package com.sos.chakhaeng.core.session

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sos.chakhaeng.BuildConfig
import com.sos.chakhaeng.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject

class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val credentialManager: CredentialManager = CredentialManager.create(appContext)
) {

    companion object {
        private const val TAG = "GoogleAuth"
    }

    // 승인된 계정만 / 전체 계정 허용을 분기해서 옵션 구성
    private fun googleIdOption(authorizedOnly: Boolean) =
        GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID) // ✅ Web Client ID 사용
            .setFilterByAuthorizedAccounts(authorizedOnly)
            .setAutoSelectEnabled(true) // 단일 자격 자동 선택(선택사항)
            .build()

    private fun credentialRequest(authorizedOnly: Boolean) =
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption(authorizedOnly))
            .build()

    /**
     * Google One-Tap 로그인 시도 후 (idToken, User) 반환. 실패 시 (null, null).
     * ctx는 어떤 Context든 가능. 내부에서 Activity를 찾아 사용.
     */
    suspend fun signInWithGoogle(ctx: Context): Pair<String?, User?> {
        val activity = ctx.findActivity() ?: run {
            Log.e(TAG, "signInWithGoogle requires an Activity context")
            return null to null
        }

        // 1차: 이미 승인된 계정만 → 실패 시 2차: 전체 계정 허용(웹 UI 포함)
        val googleCred = runCatching {
            credentialManager.getCredential(
                context = activity,
                request = credentialRequest(true)
            ).credential
        }.recoverCatching {
            credentialManager.getCredential(
                context = activity,
                request = credentialRequest(false)
            ).credential
        }.mapCatching { cred ->
            GoogleIdTokenCredential.createFrom(cred.data)
        }.onFailure { e ->
            Log.e(TAG, "signInWithGoogle failed", e)
        }.getOrNull() ?: return null to null

        val idToken = googleCred.idToken
        val email = decodeEmailFromIdToken(idToken).orEmpty()

        val user = User(
            id = googleCred.id,                 // Google 'sub' 고유 ID
            email = email,                      // ✅ 이메일은 토큰의 email 클레임에서
            name = googleCred.displayName.orEmpty(),
            pictureUrl = googleCred.profilePictureUri?.toString().orEmpty()
        )

        return idToken to user
    }

    // 간단한 JWT payload 디코더 (프로덕션에서는 검증 라이브러리 사용 권장)
    private fun decodeEmailFromIdToken(idToken: String?): String? {
        return try {
            if (idToken.isNullOrBlank()) return null
            val parts = idToken.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            JSONObject(payload).optString("email", null)
        } catch (_: Exception) { null }
    }

    // 어떤 Context든 Activity를 안전하게 찾아 반환
    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
