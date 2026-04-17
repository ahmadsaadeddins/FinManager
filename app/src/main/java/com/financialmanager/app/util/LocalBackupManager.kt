package com.financialmanager.app.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.financialmanager.app.data.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LocalBackupManager"
        private const val DATABASE_NAME = "app_database"
    }

    suspend fun exportDatabase(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            if (!databaseFile.exists()) {
                return@withContext Result.failure(IOException("Database file does not exist"))
            }

            // Checkpoint WAL to ensure all data is in the main database file
            try {
                val db = AppDatabase.getInstance(context)
                val sqliteDb = db.openHelper.writableDatabase
                sqliteDb.execSQL("PRAGMA wal_checkpoint(FULL);")
                sqliteDb.execSQL("PRAGMA wal_checkpoint(TRUNCATE);")
                delay(300) // Give time for file system sync
            } catch (e: Exception) {
                Log.w(TAG, "Could not checkpoint WAL, continuing with export anyway", e)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(databaseFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(IOException("Could not open destination for writing"))

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export database", e)
            Result.failure(e)
        }
    }

    suspend fun importDatabase(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            val databaseDir = databaseFile.parentFile
            
            if (databaseDir != null && !databaseDir.exists()) {
                databaseDir.mkdirs()
            }

            // Close and clear the database instance before restoring
            AppDatabase.closeInstance()
            delay(200)

            // Delete existing database files to ensure clean restore
            val walFile = java.io.File(databaseDir, "${DATABASE_NAME}-wal")
            val shmFile = java.io.File(databaseDir, "${DATABASE_NAME}-shm")
            val journalFile = java.io.File(databaseDir, "${DATABASE_NAME}-journal")

            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
            if (journalFile.exists()) journalFile.delete()
            if (databaseFile.exists()) databaseFile.delete()

            // Copy from Uri to DB path
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(databaseFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(IOException("Could not read from selected file"))

            // Verify integrity
            try {
                val testDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                val tables = testDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                val tableCount = tables.count
                tables.close()
                testDb.close()
                
                if (tableCount == 0) {
                    return@withContext Result.failure(IOException("Imported database is empty or invalid"))
                }
            } catch (e: Exception) {
                return@withContext Result.failure(IOException("Invalid database file: ${e.message}"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import database", e)
            Result.failure(e)
        }
    }
}
