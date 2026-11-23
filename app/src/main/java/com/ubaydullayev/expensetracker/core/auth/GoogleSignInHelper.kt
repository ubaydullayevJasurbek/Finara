package com.ubaydullayev.expensetracker.core.auth

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ubaydullayev.expensetracker.R
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(
    private val fragment: Fragment,
    private val auth: FirebaseAuth
) {

    private val credentialManager =
        CredentialManager.create(fragment.requireContext())

    /** Google orqali kiradi va kirgan foydalanuvchini qaytaradi. */
    suspend fun signIn(): FirebaseUser {
        val result = auth.signInWithCredential(getFirebaseCredential()).await()
        return result.user ?: error("Google sign-in returned no user")
    }

    /**
     * Google akkaunt tanlash oynasini ochib, Firebase credential'ini qaytaradi.
     * Kirish uchun ham, akkauntni o'chirishdan oldin qayta tasdiqlash uchun ham ishlatiladi.
     */
    suspend fun getFirebaseCredential(): AuthCredential {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(fragment.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential = credentialManager.getCredential(fragment.requireContext(), request).credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Unexpected credential")
        }

        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        return GoogleAuthProvider.getCredential(token, null)
    }
}
