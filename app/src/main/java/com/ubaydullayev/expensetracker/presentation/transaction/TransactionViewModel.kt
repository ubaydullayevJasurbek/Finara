package com.ubaydullayev.expensetracker.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ubaydullayev.expensetracker.domain.model.Transaction
import com.ubaydullayev.expensetracker.domain.repository.TransactionRepository
import com.ubaydullayev.expensetracker.utils.DateFormatter
import com.ubaydullayev.expensetracker.utils.TransactionGrouper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter: StateFlow<TransactionFilter> = _selectedFilter

    val groupedTransactions: StateFlow<List<TransactionListItem>> =
        combine(
            transactionRepository.getAllTransactions(),
            _selectedFilter
        ) { transactions, filter ->
            val filtered = when(filter){
                TransactionFilter.ALL -> transactions
                TransactionFilter.TODAY -> transactions.filter { DateFormatter.isToday(it.timestamp) }
                TransactionFilter.YESTERDAY -> transactions.filter { DateFormatter.isYesterday(it.timestamp) }
                TransactionFilter.THIS_WEEK -> transactions.filter { DateFormatter.isThisWeek(it.timestamp) }
                TransactionFilter.THIS_MONTH -> transactions.filter { DateFormatter.isThisMonth(it.timestamp) }
            }
            TransactionGrouper.groupByDay(filtered)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),emptyList())
    val monthlyIncome: StateFlow<Double> =
        transactionRepository.getMonthlyIncome(DateFormatter.getCurrentMonthStart())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> =
        transactionRepository.getMonthlyExpense(DateFormatter.getCurrentMonthStart())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun selectFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }
}