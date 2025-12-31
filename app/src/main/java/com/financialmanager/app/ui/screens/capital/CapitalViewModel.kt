package com.financialmanager.app.ui.screens.capital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.CapitalTransaction
import com.financialmanager.app.data.repository.CapitalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CapitalViewModel @Inject constructor(
    private val repository: CapitalRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val transactions = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllTransactions()
        } else {
            repository.searchTransactions(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertTransaction(transaction: CapitalTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: CapitalTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: CapitalTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}

