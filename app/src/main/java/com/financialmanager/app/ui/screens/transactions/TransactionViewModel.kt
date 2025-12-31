package com.financialmanager.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.repository.InventoryRepository
import com.financialmanager.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    val transactions = combine(
        _searchQuery,
        _selectedType
    ) { query, type ->
        when {
            !query.isBlank() -> repository.searchTransactions(query)
            type != null -> repository.getTransactionsByType(type)
            else -> repository.getAllTransactions()
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setType(type: String?) {
        _selectedType.value = type
    }

    fun insertTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
            // If it's a sale, deduct quantity from inventory
            if (transaction.type == "sale" && transaction.relatedItemId != null) {
                val item = inventoryRepository.getItemById(transaction.relatedItemId)
                item?.let {
                    val updatedItem = it.copy(
                        quantity = (it.quantity - transaction.quantity).coerceAtLeast(0),
                        updatedAt = System.currentTimeMillis()
                    )
                    inventoryRepository.updateItem(updatedItem)
                }
            }
        }
    }

    fun updateTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            val oldTransaction = repository.getTransactionById(transaction.id)
            repository.updateTransaction(transaction)
            
            // Handle inventory updates for sales
            if (transaction.type == "sale" && transaction.relatedItemId != null) {
                val item = inventoryRepository.getItemById(transaction.relatedItemId)
                item?.let {
                    val oldQuantity = oldTransaction?.quantity ?: 0
                    val quantityDiff = transaction.quantity - oldQuantity
                    val updatedItem = it.copy(
                        quantity = (it.quantity - quantityDiff).coerceAtLeast(0),
                        updatedAt = System.currentTimeMillis()
                    )
                    inventoryRepository.updateItem(updatedItem)
                }
            }
        }
    }

    fun deleteTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            // If it's a sale, restore quantity to inventory
            if (transaction.type == "sale" && transaction.relatedItemId != null) {
                val item = inventoryRepository.getItemById(transaction.relatedItemId)
                item?.let {
                    val updatedItem = it.copy(
                        quantity = it.quantity + transaction.quantity,
                        updatedAt = System.currentTimeMillis()
                    )
                    inventoryRepository.updateItem(updatedItem)
                }
            }
        }
    }

    val inventoryItems = inventoryRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

