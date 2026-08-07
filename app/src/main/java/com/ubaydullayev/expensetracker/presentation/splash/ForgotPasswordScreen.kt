package com.ubaydullayev.expensetracker.presentation.splash

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.databinding.ScreenForgotPasswordBinding
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ForgotPasswordScreen : BaseFragment<ScreenForgotPasswordBinding>() {

    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenForgotPasswordBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Edge-to-edge insets + status-bar icon color are handled by BaseFragment.
        super.onViewCreated(view, savedInstanceState)

        setupInputs()
        setupActions()
    }

    /** Clear the inline error as soon as the user edits the field. */
    private fun setupInputs() = with(binding) {
        gmailEdit.doAfterTextChanged { gmailLayout.error = null }
    }

    private fun setupActions() = with(binding) {
        sendResetLinkBtn.setOnClickListener {
            val email = gmailEdit.text?.toString().orEmpty().trim()
            if (!validateEmail(email)) return@setOnClickListener
            sendResetLink(email)
        }

        // Both "Sign in" footer link and the success-state button return to login.
        signInBtn.setOnClickListener { backToLogin() }
        backToSignInBtn.setOnClickListener { backToLogin() }
    }

    private fun backToLogin() {
        findNavController().navigate(R.id.action_forgotPasswordScreen_to_loginScreen)
    }

    /** Sends the Firebase password-reset email, then swaps to the success state. */
    private fun sendResetLink(email: String) = with(binding) {
        sendResetLinkBtn.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                showSuccess(email)
            } catch (e: Exception) {
                sendResetLinkBtn.isEnabled = true
                Toast.makeText(requireContext(), mapAuthError(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Swap the request form for the confirmation state. */
    private fun showSuccess(email: String) = with(binding) {
        requestGroup.isVisible = false
        footerGroup.isVisible = false
        successSubtitle.text = getString(R.string.reset_email_sent_subtitle, email)
        successGroup.isVisible = true
    }

    private fun mapAuthError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> getString(R.string.error_user_not_found)
        else -> e.message ?: getString(R.string.error_generic)
    }

    /** Returns true when the email is present and well-formed; otherwise shows an inline error. */
    private fun validateEmail(email: String): Boolean = with(binding) {
        when {
            email.isBlank() -> {
                gmailLayout.error = getString(R.string.error_email_required)
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                gmailLayout.error = getString(R.string.error_email_invalid)
                false
            }

            else -> true
        }
    }
}
