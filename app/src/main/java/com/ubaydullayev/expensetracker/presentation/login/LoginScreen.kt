package com.ubaydullayev.expensetracker.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.core.auth.GoogleSignInHelper
import com.ubaydullayev.expensetracker.databinding.ScreenLoginBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginScreen : BaseFragment<ScreenLoginBinding>() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }

    private val googleHelper by lazy { GoogleSignInHelper(this, auth) }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenLoginBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Edge-to-edge insets + status-bar icon color are handled by BaseFragment.
        super.onViewCreated(view, savedInstanceState)

        setupInputs()
        setupPrimaryActions()
        setupSocialSignIn()

    }

    /** Clear the inline error as soon as the user edits a field. */
    private fun setupInputs() = with(binding) {
        gmailEdit.doAfterTextChanged { gmailLayout.error = null }
        passwordEdit.doAfterTextChanged { passwordLayout.error = null }
    }

    private fun setupPrimaryActions() = with(binding) {
        signInBtn.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val email = gmailEdit.text?.toString().orEmpty().trim()
            val password = passwordEdit.text?.toString().orEmpty()
            val isRememberMe = rememberMe.isChecked

            authenticate(email, password, isRememberMe)
        }

        forgotPasswordBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_forgotPasswordScreen)
        }

        signUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_loginSignUpScreen)
        }
    }

    /** Entry point for the real sign-in call. */
    private fun authenticate(email: String, password: String, isRememberMe: Boolean) =
        with(binding) {

            signInBtn.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
                } catch (e: Exception) {
                    signInBtn.isEnabled = true
                    Toast.makeText(requireContext(), mapAuthError(e), Toast.LENGTH_SHORT).show()
                }
            }

        }

    private fun mapAuthError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> "Bunday foydalanuvchi topilmadi"
        is FirebaseAuthInvalidCredentialsException -> "Email yoki parol noto'gri"
        else -> e.message ?: "Xatolik yuz berdi"
    }

    private fun setupSocialSignIn() = with(binding) {
        appleBtn.setOnClickListener { /* TODO: start Apple sign-in */ }
        facebookBtn.setOnClickListener { /* TODO: start Facebook sign-in */ }
        googleBtn.setOnClickListener { signInWithGoogle() }
        twitterBtn.setOnClickListener { /* TODO: start X (Twitter) sign-in */ }
    }

    private fun signInWithGoogle() = with(binding) {

        googleBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    googleHelper.signIn()
                    findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Returns true when both fields are non-empty; otherwise shows inline errors. */
    private fun validateInputs(): Boolean = with(binding) {
        var valid = true

        if (gmailEdit.text.isNullOrBlank()) {
            gmailLayout.error = getString(R.string.error_email_required)
            valid = false
        }
        if (passwordEdit.text.isNullOrBlank()) {
            passwordLayout.error = getString(R.string.error_password_required)
            valid = false
        }
        valid
    }
}
