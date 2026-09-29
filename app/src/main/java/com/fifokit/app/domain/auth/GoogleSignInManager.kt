package com.fifokit.app.data.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.fifokit.app.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.ClearCredentialStateRequest

class GoogleSignInManager(
    private val activity: Activity
) {

    private val credentialManager =
        CredentialManager.create(activity)

    suspend fun getGoogleIdToken(): String {
        runCatching {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
        }
        val googleOption =
            GetSignInWithGoogleOption.Builder(
                activity.getString(
                    R.string.default_web_client_id
                )
            ).build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

        val result =
            credentialManager.getCredential(
                context = activity,
                request = request
            )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            return googleCredential.idToken
        }

        throw IllegalStateException("Unexpected Google credential type")
    }
}