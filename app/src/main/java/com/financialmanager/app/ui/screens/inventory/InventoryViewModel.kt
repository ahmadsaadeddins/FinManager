package com.financialmanager.app.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.Currency
import com.financialmanager.app.data.entities.InventoryItem
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.financialmanager.app.R
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _errorState = MutableStateFlow<Pair<Int, String?>?>(null)
    val errorState: StateFlow<Pair<Int, String?>?> = _errorState.asStateFlow()

    val currency = userPreferences.currency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.EGP)

    val items = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        when {
            query.isNotBlank() -> repository.searchItems(query)
            category != null -> repository.getItemsByCategory(category)
            else -> repository.getAllItems()
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = items.map { itemList ->
        itemList.mapNotNull { it.category }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun insertItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.insertItem(item)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = R.string.failed_to_insert_item to e.message
            }
        }
    }

    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = R.string.failed_to_update_item to e.message
            }
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = R.string.failed_to_delete_item to e.message
            }
        }
    }

    fun clearError() {
        _errorState.value = null
    }
}

