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
import com.ubaydullayev.expensetracker.presentation.common.applySystemBarInsetsAsPadding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginScreen : BaseFragment<ScreenLoginBinding>() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }

    private val googleHelper by lazy { GoogleSignInHelper(this, auth) }

    // The green hero banner must bleed to the very top, behind a fully transparent status bar,
    // so we opt out of BaseFragment's root top-inset padding and drive insets ourselves below.
    override val applySystemBarInsets: Boolean = false

    // Status-bar icons are white: they sit over the dark-green banner (iOS light-content equivalent).
    override val lightStatusBars: Boolean = false

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenLoginBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Banner stays full-bleed under the transparent status bar (hero pattern → no top inset).
        // Only the scrollable form is inset, for the navigation bar and any side display cutout.
        // Heights come from live WindowInsets, so they adapt to notch / gesture nav / rotation —
        // never a hardcoded status-bar height.
        binding.formContainer.applySystemBarInsetsAsPadding(
            applyLeft = true,
            applyTop = false,
            applyRight = true,
            applyBottom = true,
        )

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
        googleBtn.setOnClickListener { signInWithGoogle() }
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
