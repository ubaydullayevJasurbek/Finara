package com.ubaydullayev.expensetracker.presentation.transactionDetail

import com.ubaydullayev.expensetracker.presentation.common.showFinara
import com.ubaydullayev.expensetracker.presentation.common.tintPositiveDanger
import com.ubaydullayev.expensetracker.presentation.common.setFinaraTitle
import com.ubaydullayev.expensetracker.presentation.common.DialogTone
import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.data.local.database.AppDatabase
import com.ubaydullayev.expensetracker.databinding.ScreenTransactionDetailBinding
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.presentation.common.BaseFragment
import com.ubaydullayev.expensetracker.utils.CategoryStyleProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.ubaydullayev.expensetracker.data.local.mapper.toDomainModel
import com.ubaydullayev.expensetracker.data.local.mapper.toEntity
import com.ubaydullayev.expensetracker.utils.formatAmount


class TransactionDetailScreen : BaseFragment<ScreenTransactionDetailBinding>() {

    private val args: TransactionDetailScreenArgs by navArgs()
    private var currentTransaction: Transaction? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ScreenTransactionDetailBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadTransaction()
        setupActions()
    }

    private fun loadTransaction() {
        val dao = AppDatabase.getInstance(requireContext()).transactionDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val entity = dao.getTransactionById(args.transactionId)
            if (entity == null) {
                findNavController().popBackStack()
                return@launch
            }

            // ENDI HAMMASI "launch" BLOKI ICHIDA
            val transaction = entity.toDomainModel()
            currentTransaction = transaction
            bindTransaction(transaction)
        }
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun bindTransaction(transaction: Transaction) {
        val style = CategoryStyleProvider.getStyle(transaction.category)


        binding.ivHeroIcon.setImageResource(style.iconRes)
        // Oq sarlavhada ikonka plitkasi kategoriya rangida
        binding.ivHeroIcon.imageTintList = ColorStateList.valueOf(style.textColor)
        binding.heroIconBg.background = GradientDrawable().apply {
            cornerRadius = 13f * resources.displayMetrics.density
            setColor(style.iconBgColor)
        }
        binding.tvHeroMerchant.text = transaction.title
        binding.tvHeroCategory.text = transaction.category

        val sign = if (transaction.isIncome) "+" else "−"
        binding.tvHeroAmount.text = "$sign${formatAmount(transaction.amount)}"
        binding.tvHeroAmount.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (transaction.isIncome) R.color.fin_success else R.color.fin_error
            )
        )

        // Details
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.tvDetailDate.text = dateFormat.format(transaction.timestamp)
        binding.tvDetailTime.text = timeFormat.format(transaction.timestamp)

        if (transaction.note.isBlank()) {
            binding.notesCard.visibility = View.GONE
        } else {
            binding.notesCard.visibility = View.VISIBLE
            binding.tvNoteContent.text = transaction.note
        }

        // Recurring badge — faqat isRecurring=true bo'lsa ko'rinadi
        binding.recurringCard.visibility =
            if (transaction.isRecurring) View.VISIBLE else View.GONE
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnEdit.setOnClickListener {
            // TODO: Edit funksiyasi — AddTransactionScreen hozircha faqat "yangi qo'shish"
            // rejimida ishlaydi, tahrirlash rejimi keyinroq qo'shiladi
        }

        binding.btnDuplicate.setOnClickListener { duplicateTransaction() }

        // Hero'dagi va pastdagi Share tugmalari bir xil ishlaydi
        binding.btnHeroShare.setOnClickListener { shareTransaction() }
        binding.btnShared.setOnClickListener { shareTransaction() }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext(), R.style.RoundedMaterialDialog)
            .setFinaraTitle(R.string.detail_delete_title, R.drawable.ic_delete, DialogTone.DANGER)
            .setMessage(R.string.detail_delete_message)
            .setPositiveButton(R.string.detail_delete) { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton(R.string.detail_cancel, null)
            .showFinara()
            .tintPositiveDanger()
    }

    /** Nusxa hozirgi vaqt bilan yangi tranzaksiya sifatida saqlanadi. */
    private fun duplicateTransaction() {
        val transaction = currentTransaction ?: return
        val dao = AppDatabase.getInstance(requireContext()).transactionDao()

        viewLifecycleOwner.lifecycleScope.launch {
            dao.insert(transaction.copy(id = 0, timestamp = System.currentTimeMillis()).toEntity())
            Toast.makeText(requireContext(), R.string.detail_duplicated, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun shareTransaction() {
        val t = currentTransaction ?: return
        val sign = if (t.isIncome) "+" else "−"
        val date = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(t.timestamp)
        val text = buildString {
            appendLine(t.title)
            appendLine("$sign${formatAmount(t.amount)} · ${t.category}")
            appendLine(date)
            if (t.note.isNotBlank()) appendLine(t.note)
        }.trim()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.detail_share_chooser)))
    }

    private fun deleteTransaction() {
        val transaction = currentTransaction ?: return
        val dao = AppDatabase.getInstance(requireContext()).transactionDao()

        viewLifecycleOwner.lifecycleScope.launch {
            dao.delete(transaction.toEntity())
            findNavController().popBackStack()
        }
    }
}