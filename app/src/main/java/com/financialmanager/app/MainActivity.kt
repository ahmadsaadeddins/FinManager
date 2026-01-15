package com.financialmanager.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.service.GoogleDriveBackupService
import com.financialmanager.app.ui.navigation.NavGraph
import com.financialmanager.app.ui.theme.FinancialManagerTheme
import com.financialmanager.app.util.BackupThrottler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var googleDriveBackupService: GoogleDriveBackupService
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var backupThrottler: BackupThrottler
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database checksum for change detection
        backupThrottler.initializeDatabaseChecksum()
        
        setContent {
            FinancialManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Trigger backup when app goes to background
        Log.d(TAG, "App is pausing, triggering backup...")
        performAutoBackup()
    }
    
    override fun onStop() {
        super.onStop()
        // Also trigger backup when app is stopped (more reliable)
        Log.d(TAG, "App is stopping, triggering backup...")
        performAutoBackup()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Final backup attempt if the app is finishing
        if (isFinishing) {
            Log.d(TAG, "App is finishing, final backup attempt...")
            performAutoBackup()
        }
    }
    
    private fun performAutoBackup() {
        // Use a separate thread to avoid blocking the main thread
        Thread {
            var backupSuccess = false
            try {
                Log.d(TAG, "Checking if auto backup should be performed...")
                
                // Force WAL checkpoint before checking for changes
                try {
                    Log.d(TAG, "Forcing WAL checkpoint before backup check...")
                    val databaseFile = getDatabasePath("app_database")
                    if (databaseFile.exists()) {
                        val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                            databaseFile.absolutePath,
                            null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                        )
                        db.execSQL("PRAGMA wal_checkpoint(FULL);")
                        db.execSQL("PRAGMA wal_checkpoint(TRUNCATE);")
                        db.close()
                        Thread.sleep(200) // Give more time for file system sync
                        Log.d(TAG, "WAL checkpoint completed before backup check")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not checkpoint WAL before backup check: ${e.message}")
                }
                
                // Check throttling
                if (!backupThrottler.shouldAllowAutoBackup("MainActivity")) {
                    return@Thread
                }
                
                // Use runBlocking to get preferences synchronously
                val autoBackupEnabled = kotlinx.coroutines.runBlocking {
                    userPreferences.autoBackupEnabled.first()
                }
                
                if (!autoBackupEnabled) {
                    Log.d(TAG, "Auto backup is disabled")
                    backupThrottler.markAutoBackupCompleted(false, "MainActivity")
                    return@Thread
                }
                
                // Check if user has signed in to Google Drive
                val accountName = kotlinx.coroutines.runBlocking {
                    userPreferences.googleAccountName.first()
                }
                
                if (accountName.isNullOrEmpty()) {
                    Log.d(TAG, "No Google account configured for backup")
                    backupThrottler.markAutoBackupCompleted(false, "MainActivity")
                    return@Thread
                }
                
                // Initialize Drive service
                googleDriveBackupService.initializeDriveService(accountName)
                
                Log.d(TAG, "Starting automatic backup...")
                val result = kotlinx.coroutines.runBlocking {
                    googleDriveBackupService.uploadDatabase(isAutoBackup = true)
                }
                
                if (result.isSuccess) {
                    Log.d(TAG, "Automatic backup completed successfully: ${result.getOrNull()}")
                    backupSuccess = true
                } else {
                    Log.e(TAG, "Automatic backup failed: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during automatic backup", e)
            } finally {
                backupThrottler.markAutoBackupCompleted(backupSuccess, "MainActivity")
            }
        }.apply {
            // Set as daemon thread so it doesn't prevent app from closing
            isDaemon = true
            start()
        }
    }
}

