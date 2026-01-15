package com.financialmanager.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.service.GoogleDriveBackupService
import com.financialmanager.app.util.BackupThrottler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val googleDriveBackupService: GoogleDriveBackupService,
    private val userPreferences: UserPreferences,
    private val backupThrottler: BackupThrottler
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "AutoBackupWorker"
        const val WORK_NAME = "auto_backup_work"
    }

    override suspend fun doWork(): Result {
        var backupSuccess = false
        return try {
            Log.d(TAG, "AutoBackupWorker started")
            
            // Check throttling first
            if (!backupThrottler.shouldAllowAutoBackup("WorkManager")) {
                return Result.success()
            }
            
            // Check if auto backup is enabled
            val autoBackupEnabled = userPreferences.autoBackupEnabled.first()
            if (!autoBackupEnabled) {
                Log.d(TAG, "Auto backup is disabled, skipping")
                backupThrottler.markAutoBackupCompleted(false, "WorkManager")
                return Result.success()
            }
            
            // Check if user has signed in to Google Drive
            val accountName = userPreferences.googleAccountName.first()
            if (accountName.isNullOrEmpty()) {
                Log.d(TAG, "No Google account configured for backup, skipping")
                backupThrottler.markAutoBackupCompleted(false, "WorkManager")
                return Result.success()
            }
            
            // Initialize Drive service
            googleDriveBackupService.initializeDriveService(accountName)
            
            Log.d(TAG, "Starting automatic backup via WorkManager...")
            val result = googleDriveBackupService.uploadDatabase(isAutoBackup = true)
            
            if (result.isSuccess) {
                Log.d(TAG, "WorkManager backup completed successfully: ${result.getOrNull()}")
                backupSuccess = true
                Result.success()
            } else {
                Log.e(TAG, "WorkManager backup failed: ${result.exceptionOrNull()?.message}")
                // Don't retry on certain errors like 404
                val exception = result.exceptionOrNull()
                if (exception?.message?.contains("404") == true) {
                    Log.d(TAG, "404 error, not retrying")
                    Result.success() // Don't retry 404 errors
                } else {
                    Result.retry()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during WorkManager backup", e)
            Result.retry()
        } finally {
            backupThrottler.markAutoBackupCompleted(backupSuccess, "WorkManager")
        }
    }
}