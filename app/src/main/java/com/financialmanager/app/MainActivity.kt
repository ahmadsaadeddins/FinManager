package com.financialmanager.app

import android.content.Context
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
import com.financialmanager.app.util.BackupExecutor
import com.financialmanager.app.util.BackupThrottler
import com.financialmanager.app.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var googleDriveBackupService: GoogleDriveBackupService
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    @Inject
    lateinit var backupThrottler: BackupThrottler
    
    @Inject
    lateinit var backupExecutor: BackupExecutor
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun attachBaseContext(newBase: Context) {
        // Apply saved language before activity is created
        val savedLanguage = LocaleHelper.getSavedLanguage(newBase)
        val contextWithLocale = LocaleHelper.setLocale(newBase, savedLanguage)
        super.attachBaseContext(contextWithLocale)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply locale again after activity is created
        val savedLanguage = LocaleHelper.getSavedLanguage(this)
        LocaleHelper.updateActivityLocale(this, savedLanguage)
        
        // Initialize database checksum for change detection
        lifecycleScope.launch(Dispatchers.IO) {
            backupThrottler.initializeDatabaseChecksum()
        }
        
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
        // Use lifecycleScope with Dispatchers.IO for proper coroutine-based async operations
        lifecycleScope.launch(Dispatchers.IO) {
            backupExecutor.performAutoBackup("MainActivity")
        }
    }
}


