package com.financialmanager.app.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.service.GoogleDriveBackupService
import com.google.api.services.drive.model.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackupUiState {
    object Idle : BackupUiState()
    object SigningIn : BackupUiState()
    data class SignedIn(val accountName: String) : BackupUiState()
    object CreatingBackup : BackupUiState()
    data class BackupSuccess(val message: String) : BackupUiState()
    data class BackupError(val message: String) : BackupUiState()
    object ListingBackups : BackupUiState()
    data class BackupsListed(val backups: List<File>) : BackupUiState()
    object Restoring : BackupUiState()
    data class RestoreSuccess(val message: String) : BackupUiState()
    data class RestoreError(val message: String) : BackupUiState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupService: GoogleDriveBackupService
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _backups = MutableStateFlow<List<File>>(emptyList())
    val backups: StateFlow<List<File>> = _backups.asStateFlow()

    private val _accountEmail = MutableStateFlow<String?>(null)
    val accountEmail: StateFlow<String?> = _accountEmail.asStateFlow()

    fun signIn(accountName: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.SigningIn
            try {
                backupService.initializeDriveService(accountName)
                _accountEmail.value = accountName
                _uiState.value = BackupUiState.SignedIn(accountName)
            } catch (e: Exception) {
                _uiState.value = BackupUiState.BackupError("Sign-in failed: ${e.message}")
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.CreatingBackup
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.BackupError("Please sign in first")
                    return@launch
                }

                val folderId = backupService.createBackupFolder()
                val result = backupService.uploadDatabase(folderId)
                
                result.fold(
                    onSuccess = { message ->
                        _uiState.value = BackupUiState.BackupSuccess(message)
                        loadBackups()
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.BackupError("Backup failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.BackupError("Backup failed: ${e.message}")
            }
        }
    }

    fun loadBackups() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.ListingBackups
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.BackupError("Please sign in first")
                    return@launch
                }

                val result = backupService.listBackups()
                result.fold(
                    onSuccess = { backupsList ->
                        _backups.value = backupsList
                        _uiState.value = BackupUiState.BackupsListed(backupsList)
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.BackupError("Failed to load backups: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.BackupError("Failed to load backups: ${e.message}")
            }
        }
    }

    fun restoreBackup(fileId: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Restoring
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.BackupError("Please sign in first")
                    return@launch
                }

                val result = backupService.restoreDatabase(fileId)
                result.fold(
                    onSuccess = { message ->
                        _uiState.value = BackupUiState.RestoreSuccess(message)
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.RestoreError("Restore failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.RestoreError("Restore failed: ${e.message}")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is BackupUiState.BackupError || _uiState.value is BackupUiState.RestoreError) {
            _uiState.value = if (backupService.isInitialized()) {
                BackupUiState.SignedIn(_accountEmail.value ?: "")
            } else {
                BackupUiState.Idle
            }
        }
    }

    fun clearSuccess() {
        if (_uiState.value is BackupUiState.BackupSuccess || _uiState.value is BackupUiState.RestoreSuccess) {
            _uiState.value = if (backupService.isInitialized()) {
                BackupUiState.SignedIn(_accountEmail.value ?: "")
            } else {
                BackupUiState.Idle
            }
        }
    }

    fun isSignedIn(): Boolean = backupService.isInitialized()
}


