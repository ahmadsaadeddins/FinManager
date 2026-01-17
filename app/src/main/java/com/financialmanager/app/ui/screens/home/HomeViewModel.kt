package com.financialmanager.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.Currency
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

    // Profit formula: Total Sales - Total COGS - Total Expenses
    val profitLoss = combine(
        totalSales,
        transactionRepository.getTotalCOGS(),
        totalExpenses
    ) { sales, cogs, expenses ->
        val salesAmount = sales ?: 0.0
        val cogsAmount = cogs ?: 0.0
        val expensesAmount = expenses ?: 0.0
        salesAmount - cogsAmount - expensesAmount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // General Profit/Business Value formula: (Inventory + People Balance + Sales - Expenses) - Capital
    val generalProfit = combine(
        totalInventoryValue,
        totalPeopleBalance,
        totalSales,
        totalExpenses,
        totalCapital
    ) { inventory, people, sales, expenses, capital ->
        val inv = inventory ?: 0.0
        val ppl = people ?: 0.0
        val sls = sales ?: 0.0
        val exp = expenses ?: 0.0
        val cap = capital ?: 0.0
        (inv + ppl + sls - exp) - cap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User preferences for hiding numbers
    val hideNumbers = userPreferences.hideNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // User preferences for currency
    val currency = userPreferences.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.EGP)

    fun toggleHideNumbers() {
        viewModelScope.launch {
            val currentValue = hideNumbers.value
            userPreferences.setHideNumbers(!currentValue)
        }
    }
}