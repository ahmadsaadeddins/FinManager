package com.financialmanager.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.RecentOperation
import com.financialmanager.app.data.repository.RecentOperationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecentOperationsUiState {
    object Loading : RecentOperationsUiState()
    data class Success(val operations: List<RecentOperation>) : RecentOperationsUiState()
    data class Error(val message: String) : RecentOperationsUiState()
    data class DeleteSuccess(val message: String) : RecentOperationsUiState()
    data class DeleteError(val message: String) : RecentOperationsUiState()
}

@HiltViewModel
class RecentOperationsViewModel @Inject constructor(
    private val repository: RecentOperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecentOperationsUiState>(RecentOperationsUiState.Loading)
    val uiState: StateFlow<RecentOperationsUiState> = _uiState.asStateFlow()

    private val _operations = MutableStateFlow<List<RecentOperation>>(emptyList())
    val operations: StateFlow<List<RecentOperation>> = _operations.asStateFlow()

    init {
        loadRecentOperations()
    }

    fun loadRecentOperations(limit: Int = 50) {
        viewModelScope.launch {
            try {
                repository.getRecentOperations(limit).collect { operationsList ->
                    _operations.value = operationsList
                    _uiState.value = RecentOperationsUiState.Success(operationsList)
                }
            } catch (e: Exception) {
                _uiState.value = RecentOperationsUiState.Error("Failed to load recent operations: ${e.message}")
            }
        }
    }

    fun deleteOperation(operation: RecentOperation) {
        viewModelScope.launch {
            try {
                val success = repository.deleteOperation(operation)
                if (success) {
                    _uiState.value = RecentOperationsUiState.DeleteSuccess("Operation deleted successfully")
                    // Refresh the list
                    loadRecentOperations()
                } else {
                    _uiState.value = RecentOperationsUiState.DeleteError("Failed to delete operation")
                }
            } catch (e: Exception) {
                _uiState.value = RecentOperationsUiState.DeleteError("Error deleting operation: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        if (_uiState.value is RecentOperationsUiState.DeleteSuccess || 
            _uiState.value is RecentOperationsUiState.DeleteError ||
            _uiState.value is RecentOperationsUiState.Error) {
            _uiState.value = RecentOperationsUiState.Success(_operations.value)
        }
    }
}