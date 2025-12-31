package com.financialmanager.app.ui.screens.inventory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.InventoryItem
import com.financialmanager.app.data.repository.InventoryRepository
import com.financialmanager.app.util.InventoryImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val items = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        when {
            !query.isBlank() -> repository.searchItems(query)
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
            repository.insertItem(item)
        }
    }

    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun importFromExcel(context: Context) {
        viewModelScope.launch {
            try {
                val items = InventoryImporter.importFromAssets(context)
                if (items.isNotEmpty()) {
                    repository.insertItems(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

