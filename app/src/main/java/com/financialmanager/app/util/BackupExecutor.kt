package com.financialmanager.app.util

import android.util.Log
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.service.GoogleDriveBackupService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupExecutor @Inject constructor(
    private val googleDriveBackupService: GoogleDriveBackupService,
    private val userPreferences: UserPreferences,
    private val backupThrottler: BackupThrottler
) {
    companion object {
        private const val TAG = "BackupExecutor"
    }

    suspend fun performAutoBackup(source: String): Result<String> {
        var backupSuccess = false
        return try {
            Log.d(TAG, "Checking if auto backup should be performed from source: $source...")
            
            // Check throttling first
            if (!backupThrottler.shouldAllowAutoBackup(source)) {
                return Result.failure(Exception("Backup throttled or database not changed"))
            }
            
            // Check if auto backup is enabled
            val autoBackupEnabled = userPreferences.autoBackupEnabled.first()
            if (!autoBackupEnabled) {
                Log.d(TAG, "Auto backup is disabled")
                backupThrottler.markAutoBackupCompleted(false, source)
                return Result.failure(Exception("Auto backup is disabled"))
            }
            
            // Check if user has signed in to Google Drive
            val accountName = userPreferences.googleAccountName.first()
            if (accountName.isNullOrEmpty()) {
                Log.d(TAG, "No Google account configured for backup")
                backupThrottler.markAutoBackupCompleted(false, source)
                return Result.failure(Exception("No Google account configured"))
            }
            
            // Initialize Drive service
            googleDriveBackupService.initializeDriveService(accountName)
            
            Log.d(TAG, "Starting automatic backup from $source...")
            val result = googleDriveBackupService.uploadDatabase(isAutoBackup = true)
            
            if (result.isSuccess) {
                Log.d(TAG, "Automatic backup from $source completed successfully")
                backupSuccess = true
            } else {
                Log.e(TAG, "Automatic backup from $source failed: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error during automatic backup from $source", e)
            Result.failure(e)
        } finally {
            backupThrottler.markAutoBackupCompleted(backupSuccess, source)
        }
    }
}
