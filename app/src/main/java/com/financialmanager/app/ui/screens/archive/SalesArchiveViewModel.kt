package com.financialmanager.app.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.Currency
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.financialmanager.app.R
import javax.inject.Inject

sealed class SalesArchiveUiState {
    object Idle : SalesArchiveUiState()
    object ArchivingInProgress : SalesArchiveUiState()
    data class ArchiveSuccess(val messageRes: Int, val archivedCount: Int) : SalesArchiveUiState()
    data class ArchiveError(val messageRes: Int, val dynamicMessage: String? = null) : SalesArchiveUiState()
    object UnarchivingInProgress : SalesArchiveUiState()
    data class UnarchiveSuccess(val messageRes: Int) : SalesArchiveUiState()
    data class UnarchiveError(val messageRes: Int, val dynamicMessage: String? = null) : SalesArchiveUiState()
}

@HiltViewModel
class SalesArchiveViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<SalesArchiveUiState>(SalesArchiveUiState.Idle)
    val uiState: StateFlow<SalesArchiveUiState> = _uiState.asStateFlow()

    val currency = userPreferences.currency.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.EGP)

    // Current (active) sales data
    val activeSales = transactionRepository.getSaleTransactions()
    val totalActiveSales = transactionRepository.getTotalSales()
    val activeSalesCount = transactionRepository.getActiveSalesCount()

    // Archived sales data
    val archivedSales = transactionRepository.getArchivedSales()
    val totalArchivedSales = transactionRepository.getTotalArchivedSales()

    fun archiveAllSales() {
        viewModelScope.launch {
            _uiState.value = SalesArchiveUiState.ArchivingInProgress
            try {
                val archivedCount = transactionRepository.archiveAllSales()
                if (archivedCount > 0) {
                    _uiState.value = SalesArchiveUiState.ArchiveSuccess(
                        R.string.archive_success_format,
                        archivedCount
                    )
                } else {
                    _uiState.value = SalesArchiveUiState.ArchiveError(R.string.no_sales_to_archive)
                }
            } catch (e: Exception) {
                _uiState.value = SalesArchiveUiState.ArchiveError(R.string.failed_to_archive_sales, e.message)
            }
        }
    }

    fun unarchiveTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            _uiState.value = SalesArchiveUiState.UnarchivingInProgress
            try {
                transactionRepository.unarchiveTransaction(transaction.id)
                _uiState.value = SalesArchiveUiState.UnarchiveSuccess(R.string.transaction_restored_successfully)
            } catch (e: Exception) {
                _uiState.value = SalesArchiveUiState.UnarchiveError(R.string.failed_to_restore_transaction, e.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = SalesArchiveUiState.Idle
    }
}