package com.ubaydullayev.expensetracker.presentation.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.ubaydullayev.expensetracker.R
import com.ubaydullayev.expensetracker.core.auth.SessionManager
import com.ubaydullayev.expensetracker.data.local.prefs.UserPreferences
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.repository.SavingGoalRepository
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel(
    private val prefs: UserPreferences,
    private val session: SessionManager,
    private val transactionRepository: TransactionRepository,
    savingGoalRepository: SavingGoalRepository
) : ViewModel() {

    /** Bir martalik natijalar (toast, ulashish, Login'ga o'tish). */
    sealed interface ProfileEvent {
        data object ExportEmpty : ProfileEvent
        data class ExportReady(val csv: String) : ProfileEvent
        data object DataReset : ProfileEvent
        data object SignedOut : ProfileEvent
        data object AccountDeleted : ProfileEvent
        data class Error(@StringRes val messageRes: Int) : ProfileEvent
    }

    private val busy = MutableStateFlow(false)

    /**
     * Har safar joriy foydalanuvchidan o'qiladi: tab holati saqlanib qolgani uchun
     * ViewModel boshqa akkaunt kirgandan keyin ham ishlatilishi mumkin.
     */
    private fun memberSince(): String? = session.currentUser?.metadata?.creationTimestamp
        ?.takeIf { it > 0 }
        ?.let { SimpleDateFormat("MMM yyyy", Locale.US).format(Date(it)) }

    private val settings = combine(
        prefs.profile,
        prefs.currencyCode,
        prefs.hideBalanceOnStart
    ) { profile, currency, hideBalance -> Triple(profile, currency, hideBalance) }

    val uiState: StateFlow<ProfileUiState> = combine(
        settings,
        transactionRepository.getAllTransactions().map { it.size },
        savingGoalRepository.getAllGoals().map { it.size },
        busy
    ) { (profile, currency, hideBalance), transactions, goals, isBusy ->
        ProfileUiState(
            name = profile.displayName,
            email = profile.email,
            initials = profile.initials,
            currencyCode = currency,
            hideBalanceOnStart = hideBalance,
            transactionCount = transactions,
            goalCount = goals,
            memberSince = memberSince(),
            busy = isBusy
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    // Channel (not SharedFlow) so an event emitted while the screen is stopped is delivered once on return.
    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    private var exportInProgress = false

    val accountEmail: String
        get() = session.currentUser?.email.orEmpty()

    fun isGoogleAccount(): Boolean = session.isGoogleAccount()

    /** false - ism bo'sh. */
    fun updateName(name: String): Boolean {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return false
        prefs.updateProfile(cleanName, prefs.profile.value.email)
        return true
    }

    fun setCurrency(code: String) = prefs.setCurrencyCode(code)

    fun setHideBalanceOnStart(hide: Boolean) = prefs.setHideBalanceOnStart(hide)

    fun signOut() = runBusy {
        session.signOut()
        _events.send(ProfileEvent.SignedOut)
    }

    fun resetAppData() = runBusy {
        session.resetAppData()
        _events.send(ProfileEvent.DataReset)
    }

    /** [credential] - qayta tasdiqlash uchun (parol yoki Google). */
    fun deleteAccount(credential: AuthCredential) = runBusy {
        session.deleteAccount(credential)
        _events.send(ProfileEvent.AccountDeleted)
    }

    fun exportData() {
        if (exportInProgress) return
        exportInProgress = true
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                val event = if (transactions.isEmpty()) {
                    ProfileEvent.ExportEmpty
                } else {
                    ProfileEvent.ExportReady(withContext(Dispatchers.Default) { buildCsv(transactions) })
                }
                _events.send(event)
            } finally {
                exportInProgress = false
            }
        }
    }

    /** Bir vaqtda faqat bitta og'ir amal; xato bo'lsa tushunarli xabar yuboriladi. */
    private fun runBusy(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                _events.send(ProfileEvent.Error(errorMessage(e)))
            } finally {
                busy.value = false
            }
        }
    }

    @StringRes
    private fun errorMessage(e: Exception): Int = when {
        e is FirebaseNetworkException -> R.string.profile_error_network
        e is FirebaseAuthException && e.errorCode == "ERROR_USER_MISMATCH" ->
            R.string.profile_error_wrong_account
        e is FirebaseAuthInvalidCredentialsException -> R.string.profile_error_wrong_password
        else -> R.string.profile_error_generic
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
