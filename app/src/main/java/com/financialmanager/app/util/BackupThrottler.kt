package com.financialmanager.app.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupThrottler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "BackupThrottler"
        private const val MIN_BACKUP_INTERVAL_MS = 30_000L // 30 seconds minimum between backups
        private const val DATABASE_NAME = "app_database"
    }
    
    private var lastAutoBackupTime = 0L
    private var lastManualBackupTime = 0L
    private var isAutoBackupInProgress = false
    private var backupProcessId: String? = null // Track which process is doing backup
    private var lastDatabaseChecksum: String? = null // Track database changes
    
    /**
     * Check if automatic backup should be allowed
     * @param processId unique identifier for the calling process
     * @return true if backup should proceed, false if it should be skipped
     */
    @Synchronized
    fun shouldAllowAutoBackup(processId: String = "unknown"): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Don't allow if backup is already in progress by any process
        if (isAutoBackupInProgress) {
            Log.d(TAG, "Auto backup already in progress by process: $backupProcessId, skipping request from: $processId")
            return false
        }
        
        // Don't allow if too soon after last auto backup
        if (currentTime - lastAutoBackupTime < MIN_BACKUP_INTERVAL_MS) {
            Log.d(TAG, "Auto backup too soon after last backup (${currentTime - lastAutoBackupTime}ms ago), skipping request from: $processId")
            return false
        }
        
        // Additional check: if last backup was very recent (within 10 seconds), skip
        // This handles rapid successive calls during app lifecycle events
        if (currentTime - lastAutoBackupTime < 10000L) {
            Log.d(TAG, "Auto backup too recent (within 10 seconds), skipping request from: $processId")
            return false
        }
        
        // Check if database has changed since last backup
        if (!hasDatabaseChanged()) {
            Log.d(TAG, "Database hasn't changed since last backup, skipping auto backup from: $processId")
            return false
        }
        
        Log.d(TAG, "Auto backup allowed for process: $processId (database has changes)")
        isAutoBackupInProgress = true
        backupProcessId = processId
        return true
    }
    
    /**
     * Check if manual backup should be allowed
     * @return true if backup should proceed, false if it should be skipped
     */
    @Synchronized
    fun shouldAllowManualBackup(): Boolean {
        Log.d(TAG, "Manual backup always allowed")
        return true
    }
    
    /**
     * Mark automatic backup as completed
     */
    @Synchronized
    fun markAutoBackupCompleted(success: Boolean, processId: String = "unknown") {
        isAutoBackupInProgress = false
        if (success) {
            lastAutoBackupTime = System.currentTimeMillis()
            // Update the database checksum after successful backup
            updateDatabaseChecksum()
            Log.d(TAG, "Auto backup completed successfully by process: $processId")
        } else {
            Log.d(TAG, "Auto backup failed by process: $processId")
        }
        backupProcessId = null
    }
    
    /**
     * Mark manual backup as completed
     */
    @Synchronized
    fun markManualBackupCompleted(success: Boolean) {
        if (success) {
            lastManualBackupTime = System.currentTimeMillis()
            // Update the database checksum after successful manual backup too
            updateDatabaseChecksum()
            Log.d(TAG, "Manual backup completed successfully")
        } else {
            Log.d(TAG, "Manual backup failed")
        }
    }
    
    /**
     * Reset throttling (useful for testing or when app restarts)
     */
    @Synchronized
    fun reset() {
        lastAutoBackupTime = 0L
        lastManualBackupTime = 0L
        isAutoBackupInProgress = false
        backupProcessId = null
        lastDatabaseChecksum = null
        Log.d(TAG, "Backup throttler reset")
    }
    
    /**
     * Check if the database has changed since the last backup
     * @return true if database has changed, false if it's the same
     */
    private fun hasDatabaseChanged(): Boolean {
        return try {
            val currentChecksum = calculateDatabaseChecksum()
            val hasChanged = currentChecksum != lastDatabaseChecksum
            
            Log.d(TAG, "Database change check:")
            Log.d(TAG, "  Current checksum: $currentChecksum")
            Log.d(TAG, "  Last backup checksum: $lastDatabaseChecksum")
            Log.d(TAG, "  Has changed: $hasChanged")
            
            hasChanged
        } catch (e: Exception) {
            Log.w(TAG, "Error checking database changes, assuming changed: ${e.message}")
            true // If we can't check, assume it changed to be safe
        }
    }
    
    /**
     * Calculate MD5 checksum of the database file
     * @return MD5 checksum string or null if file doesn't exist
     */
    private fun calculateDatabaseChecksum(): String? {
        return try {
            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            if (!databaseFile.exists()) {
                Log.d(TAG, "Database file doesn't exist: ${databaseFile.absolutePath}")
                return null
            }
            
            // Force WAL checkpoint to ensure all changes are in the main database file
            try {
                Log.d(TAG, "Forcing WAL checkpoint before checksum calculation...")
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                db.execSQL("PRAGMA wal_checkpoint(FULL);")
                db.execSQL("PRAGMA wal_checkpoint(TRUNCATE);")
                db.close()
                
                // Give a moment for file system to sync
                Thread.sleep(100)
                Log.d(TAG, "WAL checkpoint completed before checksum")
            } catch (e: Exception) {
                Log.w(TAG, "Could not checkpoint WAL before checksum, continuing anyway: ${e.message}")
            }
            
            val fileSize = databaseFile.length()
            Log.d(TAG, "Calculating checksum for database file (${fileSize} bytes)")
            
            val digest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            
            databaseFile.inputStream().use { inputStream ->
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            
            val checksum = digest.digest().joinToString("") { "%02x".format(it) }
            Log.d(TAG, "Database checksum calculated: $checksum")
            checksum
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating database checksum", e)
            null
        }
    }
    
    /**
     * Update the stored database checksum after a successful backup
     */
    private fun updateDatabaseChecksum() {
        try {
            lastDatabaseChecksum = calculateDatabaseChecksum()
            Log.d(TAG, "Updated database checksum: $lastDatabaseChecksum")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating database checksum", e)
        }
    }
    
    /**
     * Force update the database checksum (useful for initialization)
     */
    @Synchronized
    fun initializeDatabaseChecksum() {
        updateDatabaseChecksum()
        Log.d(TAG, "Database checksum initialized: $lastDatabaseChecksum")
    }
    
    /**
     * Force a WAL checkpoint and recalculate checksum
     * Useful when you know changes were made but want to ensure they're persisted
     */
    @Synchronized
    fun forceChecksumUpdate() {
        try {
            Log.d(TAG, "Forcing database sync and checksum update...")
            
            // Force WAL checkpoint
            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            if (databaseFile.exists()) {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                db.execSQL("PRAGMA wal_checkpoint(FULL);")
                db.execSQL("PRAGMA wal_checkpoint(TRUNCATE);")
                db.close()
                Thread.sleep(150) // Give time for file system sync
            }
            
            // Recalculate checksum
            updateDatabaseChecksum()
            Log.d(TAG, "Forced checksum update completed: $lastDatabaseChecksum")
        } catch (e: Exception) {
            Log.e(TAG, "Error during forced checksum update", e)
        }
    }
}