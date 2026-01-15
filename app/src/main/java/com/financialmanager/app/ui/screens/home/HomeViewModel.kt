package com.financialmanager.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val capitalRepository: CapitalRepository,
    private val inventoryRepository: InventoryRepository,
    private val transactionRepository: TransactionRepository,
    private val personRepository: PersonRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val totalCapital = capitalRepository.getTotalCapital()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalInventoryValue = inventoryRepository.getTotalInventoryValue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalExpenses = transactionRepository.getTotalExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalSales = transactionRepository.getTotalSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalPeopleBalance = personRepository.getTotalPeopleBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Profit/loss formula: Inventory Value - People Balance - Expenses - Capital
    // Note: People Balance and Expenses are inverted in the calculation since they represent
    // amounts owed to people and money spent (negative cash flow), respectively.
    // This combine operation is safe from memory leaks as it's immediately converted to StateFlow.
    val profitLoss = combine(
        totalInventoryValue, 
        totalPeopleBalance, 
        totalExpenses,
        totalCapital
    ) { inventoryValue, peopleBalance, expenses, capital ->
        val inventory = inventoryValue ?: 0.0
        val people = -(peopleBalance ?: 0.0) // Invert people balance for calculation only
        val expensesAmount = -(expenses ?: 0.0) // Invert expenses for calculation only
        val capitalAmount = capital ?: 0.0
        (inventory + people + expensesAmount) - capitalAmount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User preferences for hiding numbers
    val hideNumbers = userPreferences.hideNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleHideNumbers() {
        viewModelScope.launch {
            val currentValue = hideNumbers.value
            userPreferences.setHideNumbers(!currentValue)
        }
    }
}