package com.ubaydullayev.expensetracker.presentation.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel(
    private val prefs: UserPreferences,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    /** One-shot results of [exportData]. */
    sealed interface ExportEvent {
        data object Empty : ExportEvent
        data class Ready(val csv: String) : ExportEvent
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        prefs.profile,
        prefs.notificationsEnabled,
        prefs.currencyCode
    ) { profile, notifications, currency ->

        ProfileUiState(
            name = profile.name,
            email = profile.email,
            initials = profile.initials,
            notificationsEnabled = notifications,
            currencyCode = currency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    // Channel (not SharedFlow) so an event emitted while the screen is stopped is delivered once on return.
    private val _exportEvents = Channel<ExportEvent>(Channel.BUFFERED)
    val exportEvents: Flow<ExportEvent> = _exportEvents.receiveAsFlow()

    private var exportInProgress = false

    fun updateProfile(name: String, email: String): Boolean {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isEmpty()) return false
        if (cleanEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return false
        }
        prefs.updateProfile(cleanName, cleanEmail)
        return true
    }
    fun setNotificationsEnabled(enabled: Boolean) = prefs.setNotificationsEnabled(enabled)

    fun setCurrency(code: String) = prefs.setCurrencyCode(code)

    fun exportData() {
        if (exportInProgress) return
        exportInProgress = true
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                val event = if (transactions.isEmpty()) {
                    ExportEvent.Empty
                } else {
                    ExportEvent.Ready(withContext(Dispatchers.Default) { buildCsv(transactions) })
                }
                _exportEvents.send(event)
            } finally {
                exportInProgress = false
            }
        }
    }

    private fun buildCsv(transactions: List<Transaction>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val currency = prefs.currencyCode.value
        return buildString {
            appendLine("Date,Title,Category,Type,Amount,Currency,Recurring,Note")
            transactions.sortedByDescending { it.timestamp }.forEach { t ->
                appendLine(
                    listOf(
                        dateFormat.format(Date(t.timestamp)),
                        t.title,
                        t.category,
                        if (t.isIncome) "Income" else "Expense",
                        String.format(Locale.US, "%.2f", t.amount),
                        currency,
                        if (t.isRecurring) "Yes" else "No",
                        t.note
                    ).joinToString(",") { it.csvEscape() }
                )
            }
        }
    }

    private fun String.csvEscape(): String =
        if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + replace("\"", "\"\"") + "\""
        } else this
}
