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
import com.financialmanager.app.util.BackupExecutor
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
    
    @Inject
    lateinit var backupExecutor: BackupExecutor
    
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
            backupExecutor.performAutoBackup("Application")
        }
    }
}