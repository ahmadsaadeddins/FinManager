package com.financialmanager.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.service.GoogleDriveBackupService
import com.financialmanager.app.util.BackupThrottler
import com.financialmanager.app.worker.AutoBackupWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FinancialManagerApplication : Application(), DefaultLifecycleObserver, Configuration.Provider {
    
    @Inject
    lateinit var googleDriveBackupService: GoogleDriveBackupService
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var backupThrottler: BackupThrottler
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "FinancialManagerApp"
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onCreate() {
        super<Application>.onCreate()
        
        // Register for app lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "Application created and lifecycle observer registered")
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        // This is called when the app goes to background
        Log.d(TAG, "App moved to background - MainActivity should handle backup, skipping Application backup")
        
        // Don't do backup from Application lifecycle - let MainActivity handle it
        // MainActivity onPause/onStop should be sufficient
        
        // Don't schedule WorkManager backup since it's disabled
        Log.d(TAG, "WorkManager backup disabled - using MainActivity lifecycle backup only")
    }
    
    private fun scheduleBackupWorker() {
        // Temporarily disable WorkManager backup since MainActivity and Application lifecycle backups are working
        Log.d(TAG, "WorkManager backup disabled - using lifecycle backups instead")
        /*
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val backupWorkRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
                .setConstraints(constraints)
                .setInitialDelay(2, TimeUnit.SECONDS) // Small delay to let the app settle
                .build()
            
            WorkManager.getInstance(this).enqueueUniqueWork(
                AutoBackupWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                backupWorkRequest
            )
            
            Log.d(TAG, "Backup WorkManager task scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule backup worker, continuing without WorkManager", e)
            // Don't fail if WorkManager has issues, the other backup methods will still work
        }
        */
    }
    
    private fun performAutoBackup() {
        applicationScope.launch {
            var backupSuccess = false
            try {
                Log.d(TAG, "Checking if auto backup should be performed...")
                
                // Check throttling first
                if (!backupThrottler.shouldAllowAutoBackup("Application")) {
                    return@launch
                }
                
                // Check if auto backup is enabled
                val autoBackupEnabled = userPreferences.autoBackupEnabled.first()
                if (!autoBackupEnabled) {
                    Log.d(TAG, "Auto backup is disabled")
                    backupThrottler.markAutoBackupCompleted(false, "Application")
                    return@launch
                }
                
                // Check if user has signed in to Google Drive
                val accountName = userPreferences.googleAccountName.first()
                if (accountName.isNullOrEmpty()) {
                    Log.d(TAG, "No Google account configured for backup")
                    backupThrottler.markAutoBackupCompleted(false, "Application")
                    return@launch
                }
                
                // Initialize Drive service
                googleDriveBackupService.initializeDriveService(accountName)
                
                Log.d(TAG, "Starting automatic backup from application lifecycle...")
                val result = googleDriveBackupService.uploadDatabase(isAutoBackup = true)
                
                if (result.isSuccess) {
                    Log.d(TAG, "Automatic backup completed successfully: ${result.getOrNull()}")
                    backupSuccess = true
                } else {
                    Log.e(TAG, "Automatic backup failed: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during automatic backup", e)
            } finally {
                backupThrottler.markAutoBackupCompleted(backupSuccess, "Application")
            }
        }
    }
}