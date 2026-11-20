package com.ubaydullayev.expensetracker.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import com.ubaydullayev.expensetracker.utils.TransactionGrouper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Tanlangan oy uchun yig'indi (hero'da ko'rsatiladi). */
data class MonthSummary(
    val income: Double = 0.0,
    val expense: Double = 0.0
) {
    val net: Double get() = income - expense
}

class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter: StateFlow<TransactionFilter> = _selectedFilter.asStateFlow()

    /** 0 = joriy oy, -1 = o'tgan oy va h.k. Kelajak oylarga o'tilmaydi. */
    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset.asStateFlow()

    private val _query = MutableStateFlow("")

    /** "November 2026" */
    val monthLabel: StateFlow<String> = _monthOffset
        .map { offset ->
            SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(monthStart(offset).time)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** Faqat tanlangan oyning tranzaksiyalari. */
    private val monthTransactions =
        combine(transactionRepository.getAllTransactions(), _monthOffset) { transactions, offset ->
            val start = monthStart(offset).timeInMillis
            val end = monthStart(offset + 1).timeInMillis
            transactions.filter { it.timestamp in start until end }
        }

    val monthSummary: StateFlow<MonthSummary> = monthTransactions
        .map { list ->
            MonthSummary(
                income = list.filter { it.isIncome }.sumOf { it.amount },
                expense = list.filter { !it.isIncome }.sumOf { it.amount }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthSummary())

    val groupedTransactions: StateFlow<List<TransactionListItem>> =
        combine(monthTransactions, _selectedFilter, _query) { transactions, filter, query ->
            val byType = when (filter) {
                TransactionFilter.ALL -> transactions
                TransactionFilter.INCOME -> transactions.filter { it.isIncome }
                TransactionFilter.EXPENSE -> transactions.filter { !it.isIncome }
                TransactionFilter.RECURRING -> transactions.filter { it.isRecurring }
            }
            TransactionGrouper.groupByDay(byType.filter { it.matches(query) })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun setQuery(query: String) {
        _query.value = query.trim()
    }

    fun previousMonth() {
        _monthOffset.value -= 1
    }

    fun nextMonth() {
        if (_monthOffset.value < 0) _monthOffset.value += 1
    }

    /** Nomi, kategoriyasi yoki izohida qidiruv so'zi bo'lsa mos keladi (katta-kichik harf farqsiz). */
    private fun Transaction.matches(query: String): Boolean =
        query.isEmpty() ||
                title.contains(query, ignoreCase = true) ||
                category.contains(query, ignoreCase = true) ||
                note.contains(query, ignoreCase = true)

    private fun monthStart(offset: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, offset)
        }
}