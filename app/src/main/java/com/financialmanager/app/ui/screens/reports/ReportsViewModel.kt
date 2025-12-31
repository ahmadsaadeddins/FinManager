package com.financialmanager.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val capitalRepository: CapitalRepository,
    private val inventoryRepository: InventoryRepository,
    private val transactionRepository: TransactionRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    val totalCapital = capitalRepository.getTotalCapital()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalInventoryValue = inventoryRepository.getTotalInventoryValue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalExpenses = transactionRepository.getTotalExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalSales = transactionRepository.getTotalSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalInvestments = capitalRepository.getTotalInvestments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalWithdrawals = capitalRepository.getTotalWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

