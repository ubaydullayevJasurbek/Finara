package com.ubaydullayev.expensetracker.presentation.profile

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.core.auth.GoogleSignInHelper
import com.ubaydullayev.expensetracker.core.auth.SessionManager
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import com.ubaydullayev.expensetracker.data.repository.SavingGoalRepositoryImpl
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.DialogConfirmPasswordBinding
import com.ubaydullayev.expensetracker.databinding.DialogEditProfileBinding
import com.ubaydullayev.expensetracker.databinding.ItemProfileRowBinding
import com.ubaydullayev.expensetracker.databinding.ScreenProfileBinding
import com.ubaydullayev.expensetracker.domain.model.AppCurrency
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import com.ubaydullayev.expensetracker.presentation.profile.ProfileViewModel.ProfileEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileScreen : BaseViewModelFragment<ScreenProfileBinding, ProfileViewModel>(
    ProfileViewModel::class.java
) {

    override val insetTop: Boolean = false          // navy hero status bar tagiga cho'ziladi
    override val lightStatusBars: Boolean = false   // status bar ikonkalari oq

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenProfileBinding.inflate(inflater, container, false)

    override val viewModelFactory
        get() = AppDatabase.getInstance(requireContext()).let { db ->
            ProfileViewModelFactory(
                prefs = UserPreferences.getInstance(requireContext()),
                session = SessionManager.getInstance(requireContext()),
                transactionRepository = TransactionRepositoryImpl(db.transactionDao()),
                savingGoalRepository = SavingGoalRepositoryImpl(db.savingGoalDao())
            )
        }

    private val googleHelper by lazy { GoogleSignInHelper(this, Firebase.auth) }

    /** Ikonka foni va rangi: oddiy qatorlar ko'k, xavfli amallar to'q sariq yoki qizil. */
    private enum class RowTone(@ColorRes val bg: Int, @ColorRes val tint: Int, @ColorRes val title: Int) {
        DEFAULT(R.color.fin_blue_tint, R.color.fin_blue, R.color.fin_navy),
        WARNING(R.color.fin_orange_tint, R.color.fin_orange_text, R.color.fin_navy),
        DANGER(R.color.fin_error_tint, R.color.fin_error, R.color.fin_error)
    }

    private enum class Trailing { CHEVRON, SWITCH, NONE }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderInset()
        setupRows()
        setupActions()
        observeState()
        observeEvents()
    }

    // ---------- Sozlash ----------

    private fun setupHeaderInset() {
        val header = binding.profileHeader
        val baseTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, baseTop + top, v.paddingRight, v.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(header)
    }

    private fun setupRows() = with(binding) {
        rowCurrency.setup(R.drawable.ic_currency, R.string.profile_currency)
        rowHideBalance.setup(
            R.drawable.ic_eye_off, R.string.profile_hide_balance,
            subtitle = R.string.profile_hide_balance_sub, trailing = Trailing.SWITCH
        )
        rowExport.setup(R.drawable.ic_download, R.string.profile_export, R.string.profile_export_sub)
        rowReset.setup(
            R.drawable.ic_refresh, R.string.profile_reset, R.string.profile_reset_sub,
            tone = RowTone.WARNING
        )
        rowSupport.setup(R.drawable.ic_mail, R.string.profile_support, R.string.profile_support_sub)
        rowPrivacy.setup(R.drawable.ic_shield, R.string.profile_privacy)
        rowAbout.setup(R.drawable.ic_info, R.string.profile_about, trailing = Trailing.NONE)
        rowAbout.tvRowValue.visibility = View.VISIBLE
        rowAbout.tvRowValue.text = getString(R.string.profile_version_short, appVersion())
        rowSignOut.setup(R.drawable.ic_logout, R.string.profile_sign_out, trailing = Trailing.NONE)
        rowDeleteAccount.setup(
            R.drawable.ic_delete, R.string.profile_delete_account,
            tone = RowTone.DANGER, trailing = Trailing.NONE
        )
    }

    private fun ItemProfileRowBinding.setup(
        @DrawableRes icon: Int,
        @StringRes title: Int,
        @StringRes subtitle: Int? = null,
        tone: RowTone = RowTone.DEFAULT,
        trailing: Trailing = Trailing.CHEVRON
    ) {
        val context = root.context
        ivRowIcon.setImageResource(icon)
        ImageViewCompat.setImageTintList(
            ivRowIcon, ColorStateList.valueOf(ContextCompat.getColor(context, tone.tint))
        )
        cardRowIcon.setCardBackgroundColor(ContextCompat.getColor(context, tone.bg))
        tvRowTitle.setText(title)
        tvRowTitle.setTextColor(ContextCompat.getColor(context, tone.title))
        tvRowSubtitle.visibility = if (subtitle != null) View.VISIBLE else View.GONE
        subtitle?.let { tvRowSubtitle.setText(it) }
        switchRow.visibility = if (trailing == Trailing.SWITCH) View.VISIBLE else View.GONE
        ivChevron.visibility = if (trailing == Trailing.CHEVRON) View.VISIBLE else View.GONE
    }

    private fun setupActions() = with(binding) {
        btnEditProfile.setOnClickListener { showEditNameDialog() }

        rowCurrency.root.setOnClickListener { showCurrencyDialog() }
        rowHideBalance.root.setOnClickListener { rowHideBalance.switchRow.toggle() }
        rowHideBalance.switchRow.setOnCheckedChangeListener { _, checked ->
            viewModel.setHideBalanceOnStart(checked)
        }

        rowExport.root.setOnClickListener { viewModel.exportData() }
        rowReset.root.setOnClickListener { confirmResetData() }
        rowSupport.root.setOnClickListener { sendSupportEmail() }
        rowPrivacy.root.setOnClickListener { openUrl(PRIVACY_POLICY_URL) }
        rowAbout.root.setOnClickListener { showAboutDialog() }
        rowSignOut.root.setOnClickListener { confirmSignOut() }
        rowDeleteAccount.root.setOnClickListener { confirmDeleteAccount() }
    }

    // ---------- Holat ----------

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: ProfileUiState) = with(binding) {
        tvInitials.text = state.initials
        tvName.text = state.name
        tvEmail.text = state.email
        tvEmail.visibility = if (state.email.isEmpty()) View.GONE else View.VISIBLE

        tvStatTransactions.text = state.transactionCount?.toString() ?: DASH
        tvStatGoals.text = state.goalCount?.toString() ?: DASH
        tvStatSince.text = state.memberSince ?: DASH

        val currency = AppCurrency.fromCode(state.currencyCode)
        rowCurrency.tvRowValue.visibility = View.VISIBLE
        rowCurrency.tvRowValue.text = getString(R.string.profile_currency_value, currency.code, currency.symbol)

        if (rowHideBalance.switchRow.isChecked != state.hideBalanceOnStart) {
            rowHideBalance.switchRow.isChecked = state.hideBalanceOnStart
        }

        progressBusy.visibility = if (state.busy) View.VISIBLE else View.GONE
        listOf(rowReset, rowSignOut, rowDeleteAccount).forEach {
            it.root.isEnabled = !state.busy
            it.root.alpha = if (state.busy) 0.5f else 1f
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        ProfileEvent.ExportEmpty -> toast(R.string.profile_export_empty)
                        is ProfileEvent.ExportReady -> shareCsv(event.csv)
                        ProfileEvent.DataReset -> toast(R.string.profile_reset_done)
                        ProfileEvent.SignedOut -> goToLogin()
                        ProfileEvent.AccountDeleted -> {
                            toast(R.string.profile_delete_done)
                            goToLogin()
                        }
                        is ProfileEvent.Error -> toast(event.messageRes)
                    }
                }
            }
        }
    }

    private fun goToLogin() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.profileScreen) {
            navController.navigate(R.id.action_global_loginScreen)
        }
    }

    // ---------- Dialoglar ----------

    private fun showEditNameDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        dialogBinding.etName.setText(viewModel.uiState.value.name)
        dialogBinding.etName.doAfterTextChanged { dialogBinding.tilName.error = null }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_edit_title)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.profile_cancel, null)
            .setPositiveButton(R.string.profile_save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                if (viewModel.updateName(dialogBinding.etName.text.toString())) {
                    dialog.dismiss()
                } else {
                    dialogBinding.tilName.error = getString(R.string.profile_name_required)
                }
            }
        }
        dialog.show()
    }

    private fun showCurrencyDialog() {
        val currencies = AppCurrency.ALL
        val labels = currencies.map { "${it.name} (${it.symbol})" }.toTypedArray()
        val checked = currencies.indexOfFirst { it.code == viewModel.uiState.value.currencyCode }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_currency)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                viewModel.setCurrency(currencies[which].code)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.profile_cancel, null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.profile_about_message, appVersion()))
            .setPositiveButton(R.string.profile_ok, null)
            .show()
    }

    private fun confirmSignOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_sign_out_title)
            .setMessage(R.string.profile_sign_out_message)
            .setNegativeButton(R.string.profile_cancel, null)
            .setPositiveButton(R.string.profile_sign_out) { _, _ -> viewModel.signOut() }
            .show()
    }

    private fun confirmResetData() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_reset_title)
            .setMessage(R.string.profile_reset_message)
            .setNegativeButton(R.string.profile_cancel, null)
            .setPositiveButton(R.string.profile_reset_confirm) { _, _ -> viewModel.resetAppData() }
            .show()
    }

    /** 1-qadam: ogohlantirish. 2-qadam: shaxsni tasdiqlash (parol yoki Google). */
    private fun confirmDeleteAccount() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_delete_title)
            .setMessage(R.string.profile_delete_message)
            .setNegativeButton(R.string.profile_cancel, null)
            .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                if (viewModel.isGoogleAccount()) reauthenticateWithGoogle() else askPassword()
            }
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.fin_error))
    }

    private fun askPassword() {
        val dialogBinding = DialogConfirmPasswordBinding.inflate(layoutInflater)
        dialogBinding.etPassword.doAfterTextChanged { dialogBinding.tilPassword.error = null }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_delete_password_title)
            .setMessage(getString(R.string.profile_delete_password_message, viewModel.accountEmail))
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.profile_cancel, null)
            .setPositiveButton(R.string.profile_delete_confirm, null)
            .create()

        dialog.setOnShowListener {
            val deleteButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.fin_error))
            deleteButton.setOnClickListener {
                val password = dialogBinding.etPassword.text?.toString().orEmpty()
                if (password.isEmpty()) {
                    dialogBinding.tilPassword.error = getString(R.string.profile_password_required)
                    return@setOnClickListener
                }
                dialog.dismiss()
                viewModel.deleteAccount(
                    EmailAuthProvider.getCredential(viewModel.accountEmail, password)
                )
            }
        }
        dialog.show()
    }

    private fun reauthenticateWithGoogle() {
        viewLifecycleOwner.lifecycleScope.launch {
            val credential = try {
                googleHelper.getFirebaseCredential()
            } catch (e: GetCredentialCancellationException) {
                return@launch // foydalanuvchi oynani yopdi
            } catch (e: Exception) {
                toast(R.string.profile_error_generic)
                return@launch
            }
            viewModel.deleteAccount(credential)
        }
    }

    // ---------- Tashqi harakatlar ----------

    private fun sendSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "Finara support (v${appVersion()})")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.profile_no_email_app)
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e: ActivityNotFoundException) {
            toast(R.string.profile_no_browser)
        }
    }

    private fun shareCsv(csv: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val context = requireContext()
            val uri = withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                val file = File(dir, "finara_transactions_$stamp.csv")
                file.writeText(csv, Charsets.UTF_8)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Finara transactions")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.profile_export_chooser)))
        }
    }

    private fun toast(@StringRes message: Int) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private fun appVersion(): String =
        try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }

    private companion object {
        const val DASH = "—"
        const val SUPPORT_EMAIL = "ubaydullayevjasurbek777@gmail.com"
        const val PRIVACY_POLICY_URL =
            "https://github.com/ubaydullayevJasurbek/Finara/blob/main/PRIVACY.md"
    }
}
