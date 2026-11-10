package com.ubaydullayev.expensetracker.presentation.profile

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import com.ubaydullayev.expensetracker.data.repository.TransactionRepositoryImpl
import com.ubaydullayev.expensetracker.databinding.DialogEditProfileBinding
import com.ubaydullayev.expensetracker.databinding.ItemProfileRowBinding
import com.ubaydullayev.expensetracker.databinding.ScreenProfileBinding
import com.ubaydullayev.expensetracker.domain.model.AppCurrency
import com.ubaydullayev.expensetracker.presentation.common.BaseViewModelFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

class ProfileScreen : BaseViewModelFragment<ScreenProfileBinding, ProfileViewModel>(
    ProfileViewModel::class.java
) {

    // ↓ BudgetScreen'dagi bilan bir xil shaklda
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenProfileBinding.inflate(inflater, container, false)

    override val viewModelFactory
        get() = ProfileViewModelFactory(
            UserPreferences.getInstance(requireContext()),
            // ↓ TransactionScreen'dagi transactionRepository qanday yaratilsa, aynan shunday
            TransactionRepositoryImpl(
                AppDatabase.getInstance(requireContext()).transactionDao()
            )
        )
    // ↑

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderInset()
        setupRows()
        setupActions()
        observeState()
        observeExport()
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

    private fun setupRows() {
        binding.rowNotifications.setup(
            iconRes = R.drawable.ic_notifications, title = "Notifications",
            subtitle = "Push, email, alerts",
            iconBg = "#FFF0C2", iconTint = "#B7791F", showSwitch = true
        )
        binding.rowCurrency.setup(
            iconRes = R.drawable.ic_currency, title = "Currency",
            iconBg = "#DDF5E8", iconTint = "#0F7B55"
        )
        binding.rowExport.setup(
            iconRes = R.drawable.ic_download, title = "Export data",
            subtitle = "CSV",
            iconBg = "#EDE9FE", iconTint = "#7C3AED"
        )
        binding.rowHelp.setup(
            iconRes = R.drawable.ic_help, title = "Help & Support",
            subtitle = "FAQ, contact us",
            iconBg = "#DBEAFE", iconTint = "#2563EB"
        )
        binding.rowAbout.setup(
            iconRes = R.drawable.ic_info, title = "About",
            subtitle = "Version ${appVersion()}",
            iconBg = "#E0E7FF", iconTint = "#4F46E5"
        )
    }

    private fun ItemProfileRowBinding.setup(
        iconRes: Int,
        title: String,
        iconBg: String,
        iconTint: String,
        subtitle: String? = null,
        showSwitch: Boolean = false
    ) {
        ivRowIcon.setImageResource(iconRes)
        ImageViewCompat.setImageTintList(ivRowIcon, ColorStateList.valueOf(iconTint.toColorInt()))
        cardRowIcon.setCardBackgroundColor(iconBg.toColorInt())
        tvRowTitle.text = title
        tvRowSubtitle.visibility = if (subtitle != null) View.VISIBLE else View.GONE
        tvRowSubtitle.text = subtitle
        switchRow.visibility = if (showSwitch) View.VISIBLE else View.GONE
        ivChevron.visibility = if (showSwitch) View.GONE else View.VISIBLE
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }

        binding.rowNotifications.root.setOnClickListener {
            binding.rowNotifications.switchRow.toggle()
        }
        binding.rowNotifications.switchRow.setOnCheckedChangeListener { _, checked ->
            viewModel.setNotificationsEnabled(checked)
        }

        binding.rowCurrency.root.setOnClickListener { showCurrencyDialog() }
        binding.rowExport.root.setOnClickListener { viewModel.exportData() }
        binding.rowHelp.root.setOnClickListener { sendSupportEmail() }
        binding.rowAbout.root.setOnClickListener { showAboutDialog() }
    }

    // ---------- Holat ----------

    @SuppressLint("SetTextI18n")
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvInitials.text = state.initials
                    binding.tvName.text = state.name
                    binding.tvEmail.text = state.email
                    binding.tvEmail.visibility =
                        if (state.email.isEmpty()) View.GONE else View.VISIBLE

                    if (binding.rowNotifications.switchRow.isChecked != state.notificationsEnabled) {
                        binding.rowNotifications.switchRow.isChecked = state.notificationsEnabled
                    }

                    val currency = AppCurrency.fromCode(state.currencyCode)
                    binding.rowCurrency.tvRowValue.visibility = View.VISIBLE
                    binding.rowCurrency.tvRowValue.text = "${currency.code} ${currency.symbol}"
                }
            }
        }
    }

    private fun observeExport() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exportEvents.collect { event ->
                    when (event) {
                        is ProfileViewModel.ExportEvent.Empty ->
                            Toast.makeText(
                                requireContext(), "No transactions to export", Toast.LENGTH_SHORT
                            ).show()

                        is ProfileViewModel.ExportEvent.Ready -> shareCsv(event.csv)
                    }
                }
            }
        }
    }

    // ---------- Dialoglar ----------

    private fun showEditProfileDialog() {
        val state = viewModel.uiState.value
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        dialogBinding.etName.setText(state.name)
        dialogBinding.etEmail.setText(state.email)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit profile")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etName.text.toString()
                val email = dialogBinding.etEmail.text.toString()

                dialogBinding.tilName.error = null
                dialogBinding.tilEmail.error = null

                when {
                    name.isBlank() -> dialogBinding.tilName.error = "Name is required"
                    !viewModel.updateProfile(name, email) ->
                        dialogBinding.tilEmail.error = "Invalid email"

                    else -> dialog.dismiss()
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
            .setTitle("Currency")
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                viewModel.setCurrency(currencies[which].code)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.app_name))
            .setMessage("Version ${appVersion()}\n\nPersonal finance manager. Your data stays on this device.")
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------- Tashqi harakatlar ----------

    private fun sendSupportEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "Support request (v${appVersion()})")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareCsv(csv: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val context = requireContext()
            val uri = withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                val file = File(dir, "transactions_$stamp.csv")
                file.writeText(csv, Charsets.UTF_8)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Transactions export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Export data"))
        }
    }

    private fun appVersion(): String =
        try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }

    private companion object {
        const val SUPPORT_EMAIL =
            "ubaydullayevjasurbek777@gmail.com" // keyin haqiqiy support emailga almashtiring
    }
}