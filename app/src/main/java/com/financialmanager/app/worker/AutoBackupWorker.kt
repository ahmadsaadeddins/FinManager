package com.financialmanager.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.service.GoogleDriveBackupService
import com.financialmanager.app.util.BackupExecutor
import com.financialmanager.app.util.BackupThrottler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val backupExecutor: BackupExecutor
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "AutoBackupWorker"
        const val WORK_NAME = "auto_backup_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "AutoBackupWorker started")
        val result = backupExecutor.performAutoBackup("WorkManager")
        return if (result.isSuccess) {
            Result.success()
        } else {
            val exception = result.exceptionOrNull()
            if (exception?.message?.contains("404") == true || 
                exception?.message?.contains("disabled") == true ||
                exception?.message?.contains("throttled") == true ||
                exception?.message?.contains("No Google account") == true) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}