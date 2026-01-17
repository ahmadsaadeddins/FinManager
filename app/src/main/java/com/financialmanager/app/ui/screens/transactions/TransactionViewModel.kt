package com.financialmanager.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.dao.TransactionInventoryDao
import com.financialmanager.app.data.entities.Currency
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.entities.TransactionType
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.data.repository.InventoryRepository
import com.financialmanager.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val inventoryRepository: InventoryRepository,
    private val transactionInventoryDao: TransactionInventoryDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    val selectedType: StateFlow<TransactionType?> = _selectedType.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val currency = userPreferences.currency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.EGP)

    val transactions = combine(
        _searchQuery,
        _selectedType
    ) { query, type ->
        when {
            query.isNotBlank() -> repository.searchTransactions(query)
            type != null -> repository.getTransactionsByType(type)
            else -> repository.getAllTransactions()
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems = inventoryRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setType(type: TransactionType?) {
        _selectedType.value = type
    }

    fun insertTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            try {
                transactionInventoryDao.insertTransactionWithInventoryUpdate(transaction)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to insert transaction: ${e.message}"
            }
        }
    }

    fun updateTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            try {
                transactionInventoryDao.updateTransactionWithInventoryUpdate(transaction)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update transaction: ${e.message}"
            }
        }
    }

    fun deleteTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            try {
                transactionInventoryDao.deleteTransactionWithInventoryRestore(transaction)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete transaction: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

