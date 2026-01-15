package com.financialmanager.app.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.service.GoogleDriveBackupService
import com.financialmanager.app.util.BackupThrottler
import com.google.api.services.drive.model.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.financialmanager.app.R
import javax.inject.Inject

sealed class BackupUiState {
    object Idle : BackupUiState()
    object SigningIn : BackupUiState()
    data class SignedIn(val accountName: String) : BackupUiState()
    object CreatingBackup : BackupUiState()
    data class BackupSuccess(val messageRes: Int, val messageArg: String? = null) : BackupUiState()
    data class BackupError(val messageRes: Int, val dynamicMessage: String? = null) : BackupUiState()
    object ListingBackups : BackupUiState()
    data class BackupsListed(val backups: List<File>) : BackupUiState()
    object Restoring : BackupUiState()
    data class RestoreSuccess(val messageRes: Int, val messageArg: String? = null) : BackupUiState()
    data class RestoreError(val messageRes: Int, val dynamicMessage: String? = null) : BackupUiState()
    object DeletingBackup : BackupUiState()
    data class DeleteSuccess(val messageRes: Int, val messageArg: String? = null) : BackupUiState()
    data class DeleteError(val messageRes: Int, val dynamicMessage: String? = null) : BackupUiState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupService: GoogleDriveBackupService,
    private val userPreferences: UserPreferences,
    private val backupThrottler: BackupThrottler
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _backups = MutableStateFlow<List<File>>(emptyList())
    val backups: StateFlow<List<File>> = _backups.asStateFlow()

    private val _accountEmail = MutableStateFlow<String?>(null)
    val accountEmail: StateFlow<String?> = _accountEmail.asStateFlow()

    // Auto backup settings
    val autoBackupEnabled = userPreferences.autoBackupEnabled

    fun signIn(accountName: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.SigningIn
            try {
                backupService.initializeDriveService(accountName)
                _accountEmail.value = accountName
                // Save account name to preferences for auto backup
                userPreferences.setGoogleAccountName(accountName)
                _uiState.value = BackupUiState.SignedIn(accountName)
            } catch (e: Exception) {
                _uiState.value = BackupUiState.BackupError(R.string.sign_in_failed, e.message)
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            // Manual backups are always allowed regardless of changes
            // Users might want to create a backup even if no changes were made
            if (!backupThrottler.shouldAllowManualBackup()) {
                _uiState.value = BackupUiState.BackupError(R.string.backup_not_allowed)
                return@launch
            }
            
            _uiState.value = BackupUiState.CreatingBackup
            var backupSuccess = false
            
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.BackupError(R.string.please_sign_in_first)
                    backupThrottler.markManualBackupCompleted(false)
                    return@launch
                }

                val folderId = backupService.createBackupFolder()
                val result = backupService.uploadDatabase(folderId, isAutoBackup = false)
                
                result.fold(
                    onSuccess = { message ->
                        _uiState.value = BackupUiState.BackupSuccess(R.string.backup_success, message)
                        backupSuccess = true
                        loadBackups()
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.BackupError(R.string.backup_failed, error.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.BackupError(R.string.backup_failed, e.message)
            } finally {
                backupThrottler.markManualBackupCompleted(backupSuccess)
            }
        }
    }

    fun loadBackups() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.ListingBackups
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.BackupError(R.string.please_sign_in_first)
                    return@launch
                }

                val result = backupService.listBackups()
                result.fold(
                    onSuccess = { backupsList ->
                        _backups.value = backupsList
                        _uiState.value = BackupUiState.BackupsListed(backupsList)
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.BackupError(R.string.failed_to_load_backups, error.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.BackupError(R.string.failed_to_load_backups, e.message)
            }
        }
    }

    fun restoreBackup(fileId: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Restoring
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.BackupError(R.string.please_sign_in_first)
                    return@launch
                }

                val result = backupService.restoreDatabase(fileId)
                result.fold(
                    onSuccess = { message ->
                        _uiState.value = BackupUiState.RestoreSuccess(R.string.restore_success, message)
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.RestoreError(R.string.restore_failed, error.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.RestoreError(R.string.restore_failed, e.message)
            }
        }
    }

    fun deleteBackup(fileId: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.DeletingBackup
            try {
                if (!backupService.isInitialized()) {
                    _uiState.value = BackupUiState.DeleteError(R.string.please_sign_in_first)
                    return@launch
                }

                val result = backupService.deleteBackup(fileId)
                result.fold(
                    onSuccess = { message ->
                        _uiState.value = BackupUiState.DeleteSuccess(R.string.delete_success, message)
                        loadBackups() // Refresh the list after deletion
                    },
                    onFailure = { error ->
                        _uiState.value = BackupUiState.DeleteError(R.string.failed_to_delete_operation, error.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState.DeleteError(R.string.failed_to_delete_operation, e.message)
            }
        }
    }

    fun clearError() {
        if (_uiState.value is BackupUiState.BackupError || 
            _uiState.value is BackupUiState.RestoreError ||
            _uiState.value is BackupUiState.DeleteError) {
            _uiState.value = if (backupService.isInitialized()) {
                BackupUiState.SignedIn(_accountEmail.value ?: "")
            } else {
                BackupUiState.Idle
            }
        }
    }

    fun clearSuccess() {
        if (_uiState.value is BackupUiState.BackupSuccess || 
            _uiState.value is BackupUiState.RestoreSuccess ||
            _uiState.value is BackupUiState.DeleteSuccess) {
            _uiState.value = if (backupService.isInitialized()) {
                BackupUiState.SignedIn(_accountEmail.value ?: "")
            } else {
                BackupUiState.Idle
            }
        }
    }

    fun isSignedIn(): Boolean = backupService.isInitialized()

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoBackupEnabled(enabled)
        }
    }
}


