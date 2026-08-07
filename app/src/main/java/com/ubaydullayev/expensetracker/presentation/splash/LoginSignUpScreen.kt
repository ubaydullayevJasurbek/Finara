package com.ubaydullayev.expensetracker.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ScreenLoginSignUpBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginSignUpScreen : BaseFragment<ScreenLoginSignUpBinding>() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val credentialManager by lazy { CredentialManager.create(requireContext()) }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenLoginSignUpBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Edge-to-edge insets + status-bar icon color are handled by BaseFragment.
        super.onViewCreated(view, savedInstanceState)

        setupInputs()
        setupPrimaryActions()
        setupSocialSignUp()
    }

    /** Clear each field's inline error as soon as the user edits it. */
    private fun setupInputs() = with(binding) {
        nameEdit.doAfterTextChanged { nameLayout.error = null }
        gmailEdit.doAfterTextChanged { gmailLayout.error = null }
        passwordEdit.doAfterTextChanged { passwordLayout.error = null }
        confirmPasswordEdit.doAfterTextChanged { confirmPasswordLayout.error = null }
    }

    private fun setupPrimaryActions() = with(binding) {
        createAccountBtn.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val name = nameEdit.text?.toString().orEmpty().trim()
            val email = gmailEdit.text?.toString().orEmpty().trim()
            val password = passwordEdit.text?.toString().orEmpty()

            register(name, email, password)
        }

        // Footer: "Already have an account? Sign in"
        signInBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginSignUpScreen_to_loginScreen)
        }
    }

    private fun setupSocialSignUp() = with(binding) {
        appleBtn.setOnClickListener { /* TODO: start Apple sign-up */ }
        facebookBtn.setOnClickListener { /* TODO: start Facebook sign-up */ }
        googleBtn.setOnClickListener { signInWithGoogle() }
        twitterBtn.setOnClickListener { /* TODO: start X (Twitter) sign-up */ }
    }

    private fun signInWithGoogle() {
        val googleIDOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIDOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(requireContext(), request)
                val credential = result.credential

                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_unexpected_credential),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(credential.data).idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                findNavController().navigate(R.id.action_loginSignUpScreen_to_homeScreen)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Entry point for the real account-creation call. */
    private fun register(name: String, email: String, password: String) = with(binding) {

        createAccountBtn.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                val profileUpdate = userProfileChangeRequest {
                    displayName = name
                }
                result.user?.updateProfile(profileUpdate)?.await()
                findNavController().navigate(R.id.action_loginSignUpScreen_to_homeScreen)
            } catch (e: Exception) {
                createAccountBtn.isEnabled = true
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }


    }

    /** Returns true when all fields are valid; otherwise shows inline errors. */
    private fun validateInputs(): Boolean = with(binding) {
        var valid = true

        if (nameEdit.text.isNullOrBlank()) {
            nameLayout.error = getString(R.string.error_name_required)
            valid = false
        }
        if (gmailEdit.text.isNullOrBlank()) {
            gmailLayout.error = getString(R.string.error_email_required)
            valid = false
        }

        val password = passwordEdit.text?.toString().orEmpty()
        val confirm = confirmPasswordEdit.text?.toString().orEmpty()

        if (password.isBlank()) {
            passwordLayout.error = getString(R.string.error_password_required)
            valid = false
        }
        if (confirm.isBlank()) {
            confirmPasswordLayout.error = getString(R.string.error_confirm_password_required)
            valid = false
        } else if (password.isNotBlank() && password != confirm) {
            confirmPasswordLayout.error = getString(R.string.error_password_mismatch)
            valid = false
        }

        valid
    }
}
