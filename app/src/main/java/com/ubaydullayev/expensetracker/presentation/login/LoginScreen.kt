package com.ubaydullayev.expensetracker.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ScreenLoginBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment


class LoginScreen : BaseFragment<ScreenLoginBinding>() {

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
            // TODO: navigate to the forgot-password flow (add destination + action to nav_graph.xml).
        }

        signUpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_loginSignUpScreen)
        }
    }

    /** Entry point for the real sign-in call. */
    private fun authenticate(email: String, password: String, isRememberMe: Boolean) {
        // TODO: replace with a real authentication call; navigate only after it succeeds.
        findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
    }

    private fun setupSocialSignIn() = with(binding) {
        appleBtn.setOnClickListener { /* TODO: start Apple sign-in */ }
        facebookBtn.setOnClickListener { /* TODO: start Facebook sign-in */ }
        googleBtn.setOnClickListener { /* TODO: start Google sign-in */ }
        twitterBtn.setOnClickListener { /* TODO: start X (Twitter) sign-in */ }
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
