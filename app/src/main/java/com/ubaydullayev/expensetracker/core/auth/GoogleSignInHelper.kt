package com.ubaydullayev.expensetracker.core.auth

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ubaydullayev.expensetracker.R
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(
    private val fragment: Fragment,
    private val auth: FirebaseAuth
) {

    private val credentialManager =
        CredentialManager.create(fragment.requireContext())

    suspend fun signIn(): Boolean {

        val googleIDOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(fragment.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIDOption)
            .build()

        val result = credentialManager.getCredential(
            fragment.requireContext(),
            request
        )

        val credential = result.credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw Exception("Unexpected credential")
        }

        val token = GoogleIdTokenCredential
            .createFrom(credential.data)
            .idToken

        val firebaseCredential =
            GoogleAuthProvider.getCredential(token, null)

        auth.signInWithCredential(firebaseCredential).await()

        return true
    }
}