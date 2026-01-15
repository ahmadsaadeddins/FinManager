package com.financialmanager.app.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.OutTransaction
import com.financialmanager.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SalesArchiveUiState {
    object Idle : SalesArchiveUiState()
    object ArchivingInProgress : SalesArchiveUiState()
    data class ArchiveSuccess(val message: String, val archivedCount: Int) : SalesArchiveUiState()
    data class ArchiveError(val message: String) : SalesArchiveUiState()
    object UnarchivingInProgress : SalesArchiveUiState()
    data class UnarchiveSuccess(val message: String) : SalesArchiveUiState()
    data class UnarchiveError(val message: String) : SalesArchiveUiState()
}

@HiltViewModel
class SalesArchiveViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SalesArchiveUiState>(SalesArchiveUiState.Idle)
    val uiState: StateFlow<SalesArchiveUiState> = _uiState.asStateFlow()

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
                        "Successfully archived $archivedCount sales transactions",
                        archivedCount
                    )
                } else {
                    _uiState.value = SalesArchiveUiState.ArchiveError("No sales transactions to archive")
                }
            } catch (e: Exception) {
                _uiState.value = SalesArchiveUiState.ArchiveError("Failed to archive sales: ${e.message}")
            }
        }
    }

    fun unarchiveTransaction(transaction: OutTransaction) {
        viewModelScope.launch {
            _uiState.value = SalesArchiveUiState.UnarchivingInProgress
            try {
                transactionRepository.unarchiveTransaction(transaction.id)
                _uiState.value = SalesArchiveUiState.UnarchiveSuccess("Transaction restored successfully")
            } catch (e: Exception) {
                _uiState.value = SalesArchiveUiState.UnarchiveError("Failed to restore transaction: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = SalesArchiveUiState.Idle
    }
}