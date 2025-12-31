package com.financialmanager.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.*
import com.financialmanager.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchResult(
    val type: String,
    val title: String,
    val subtitle: String?,
    val data: Any
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val capitalRepository: CapitalRepository,
    private val transactionRepository: TransactionRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            combine(
                inventoryRepository.searchItems(query),
                capitalRepository.searchTransactions(query),
                transactionRepository.searchTransactions(query),
                personRepository.searchPeople(query)
            ) { items, capital, transactions, people ->
                val results = mutableListOf<SearchResult>()
                
                items.forEach { item ->
                    results.add(SearchResult("inventory", item.name, item.category, item))
                }
                
                capital.forEach { transaction ->
                    results.add(SearchResult("capital", transaction.source, transaction.description, transaction))
                }
                
                transactions.forEach { transaction ->
                    results.add(SearchResult("transaction", transaction.description ?: "Transaction", transaction.category, transaction))
                }
                
                people.forEach { person ->
                    results.add(SearchResult("person", person.name, person.phone, person))
                }
                
                results
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

