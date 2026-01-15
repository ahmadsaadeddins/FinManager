package com.financialmanager.app.service

import android.content.Context
import android.util.Log
import com.financialmanager.app.data.database.AppDatabase
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "GoogleDriveBackupService"
        private const val BACKUP_FOLDER_NAME = "FinancialManagerBackups"
        private const val DATABASE_NAME = "app_database"
        private const val MIME_TYPE = "application/x-sqlite3"
    }

    private var driveService: Drive? = null

    fun initializeDriveService(accountName: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccountName = accountName
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Financial Manager")
            .build()
            .also { driveService = it }
    }

    suspend fun createBackupFolder(): String? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null

            // Check if folder already exists
            val existingFolder = findBackupFolder()
            if (existingFolder != null) {
                return@withContext existingFolder.id
            }

            // Create the folder
            val folderMetadata = File().apply {
                name = BACKUP_FOLDER_NAME
                mimeType = "application/vnd.google-apps.folder"
            }

            val folder = service.files().create(folderMetadata)
                .setFields("id")
                .execute()

            Log.d(TAG, "Created backup folder: ${folder.id}")
            folder.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup folder", e)
            null
        }
    }

    private suspend fun findBackupFolder(): File? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null

            val query = "mimeType='application/vnd.google-apps.folder' and name='$BACKUP_FOLDER_NAME' and trashed=false"
            val result = service.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            result.files.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error finding backup folder", e)
            null
        }
    }

    suspend fun uploadDatabase(folderId: String? = null, isAutoBackup: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please sign in first.")
            )

            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            Log.d(TAG, "Starting backup. Database file path: ${databaseFile.absolutePath}")
            Log.d(TAG, "Database file exists: ${databaseFile.exists()}")
            Log.d(TAG, "Database file size: ${if (databaseFile.exists()) databaseFile.length() else 0} bytes")
            
            if (!databaseFile.exists()) {
                Log.e(TAG, "Database file does not exist at: ${databaseFile.absolutePath}")
                return@withContext Result.failure(IOException("Database file not found"))
            }

            // Checkpoint WAL to ensure all data is in the main database file
            // This is important because Room uses Write-Ahead Logging (WAL) by default
            try {
                Log.d(TAG, "Checkpointing WAL file...")
                val db = AppDatabase.getInstance(context)
                val sqliteDb = db.openHelper.writableDatabase
                sqliteDb.execSQL("PRAGMA wal_checkpoint(FULL);")
                sqliteDb.execSQL("PRAGMA wal_checkpoint(TRUNCATE);") // Also truncate the WAL file
                kotlinx.coroutines.delay(300) // Give time for file system sync
                Log.d(TAG, "WAL checkpoint completed")
                Log.d(TAG, "Database file size after checkpoint: ${databaseFile.length()} bytes")
            } catch (e: Exception) {
                Log.w(TAG, "Could not checkpoint WAL, continuing with backup anyway", e)
                Log.w(TAG, "Checkpoint error: ${e.message}", e)
            }

            // Ensure we have a folder ID
            Log.d(TAG, "Creating/finding backup folder...")
            val targetFolderId = folderId ?: createBackupFolder()
            if (targetFolderId == null) {
                Log.e(TAG, "Failed to create or find backup folder")
                return@withContext Result.failure(IOException("Failed to create or find backup folder"))
            }
            Log.d(TAG, "Backup folder ID: $targetFolderId")

            // Check if backup already exists and handle based on backup type
            if (isAutoBackup) {
                // For automatic backups, always overwrite the same file
                val autoBackupFile = findAutoBackupFile(targetFolderId)
                if (autoBackupFile != null) {
                    try {
                        Log.d(TAG, "Updating existing auto backup: ${autoBackupFile.id}")
                        // Update the existing file instead of creating a new one
                        val fileName = "auto_backup_${DATABASE_NAME}.db"
                        val fileMetadata = File().apply {
                            name = fileName
                        }
                        Log.d(TAG, "Updating auto backup file: $fileName, size: ${databaseFile.length()} bytes")

                        val mediaContent = FileContent(MIME_TYPE, databaseFile)
                        val updatedFile = service.files().update(autoBackupFile.id, fileMetadata, mediaContent)
                            .setFields("id, name, size")
                            .execute()

                        Log.d(TAG, "Auto backup updated successfully!")
                        Log.d(TAG, "Updated file ID: ${updatedFile.id}")
                        Log.d(TAG, "Updated file name: ${updatedFile.name}")
                        Log.d(TAG, "Updated file size: ${updatedFile.size} bytes")
                        return@withContext Result.success("Auto backup updated successfully: ${updatedFile.name}")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update existing auto backup, will create new one: ${e.message}")
                        // Fall through to create new file
                    }
                }
                
                // Create new auto backup file
                val fileName = "auto_backup_${DATABASE_NAME}.db"
                val fileMetadata = File().apply {
                    name = fileName
                    parents = listOf(targetFolderId)
                }
                Log.d(TAG, "Creating new auto backup file: $fileName, size: ${databaseFile.length()} bytes")

                val mediaContent = FileContent(MIME_TYPE, databaseFile)
                val uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, size")
                    .execute()

                Log.d(TAG, "Auto backup created successfully!")
                Log.d(TAG, "Created file ID: ${uploadedFile.id}")
                Log.d(TAG, "Created file name: ${uploadedFile.name}")
                Log.d(TAG, "Created file size: ${uploadedFile.size} bytes")
                Result.success("Auto backup created successfully: ${uploadedFile.name}")
            } else {
                // For manual backups, create new files with timestamps (existing behavior)
                findExistingBackup(targetFolderId)?.let { existingFile ->
                    try {
                        Log.d(TAG, "Deleting existing manual backup: ${existingFile.id}")
                        service.files().delete(existingFile.id).execute()
                        Log.d(TAG, "Deleted existing manual backup: ${existingFile.id}")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not delete existing manual backup (file may not exist): ${e.message}")
                        // Continue with backup creation even if deletion fails
                    }
                }

                // Create file metadata with timestamp
                val fileName = "${DATABASE_NAME}_${System.currentTimeMillis()}.db"
                val fileMetadata = File().apply {
                    name = fileName
                    parents = listOf(targetFolderId)
                }
                Log.d(TAG, "Creating manual backup file: $fileName, size: ${databaseFile.length()} bytes")

                // Upload the file
                val mediaContent = FileContent(MIME_TYPE, databaseFile)
                val uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, size")
                    .execute()

                Log.d(TAG, "Manual backup created successfully!")
                Log.d(TAG, "Created file ID: ${uploadedFile.id}")
                Log.d(TAG, "Created file name: ${uploadedFile.name}")
                Log.d(TAG, "Created file size: ${uploadedFile.size} bytes")
                Result.success("Manual backup created successfully: ${uploadedFile.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading database", e)
            Result.failure(e)
        }
    }

    private suspend fun findAutoBackupFile(folderId: String): File? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null

            val query = "'$folderId' in parents and name = 'auto_backup_${DATABASE_NAME}.db' and trashed=false"
            val result = service.files().list()
                .setQ(query)
                .setFields("files(id, name, modifiedTime)")
                .execute()

            result.files.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error finding auto backup file", e)
            null
        }
    }

    private suspend fun findExistingBackup(folderId: String): File? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null

            // Only look for manual backups (timestamped files), not auto backups
            val query = "'$folderId' in parents and name contains '$DATABASE_NAME' and name contains '_' and not name = 'auto_backup_${DATABASE_NAME}.db' and trashed=false"
            val result = service.files().list()
                .setQ(query)
                .setOrderBy("modifiedTime desc")
                .setFields("files(id, name, modifiedTime)")
                .execute()

            result.files.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error finding existing manual backup", e)
            null
        }
    }

    suspend fun listBackups(): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please sign in first.")
            )

            val folderId = findBackupFolder()?.id ?: return@withContext Result.success(emptyList())

            val query = "'$folderId' in parents and name contains '$DATABASE_NAME' and trashed=false"
            val result = service.files().list()
                .setQ(query)
                .setOrderBy("modifiedTime desc")
                .setFields("files(id, name, modifiedTime, size)")
                .execute()

            Result.success(result.files)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing backups", e)
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Downloading backup file. File ID: $fileId")
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please sign in first.")
            )

            // Get file metadata first
            val fileMetadata = service.files().get(fileId).setFields("id, name, size").execute()
            Log.d(TAG, "File metadata - ID: ${fileMetadata.id}, Name: ${fileMetadata.name}, Size: ${fileMetadata.size}")

            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            val backupFile = java.io.File(databaseFile.parent, "${DATABASE_NAME}.backup")
            
            Log.d(TAG, "Download location: ${backupFile.absolutePath}")

            // Delete existing backup file if it exists
            if (backupFile.exists()) {
                Log.d(TAG, "Deleting existing backup file")
                backupFile.delete()
            }

            // Download the file
            Log.d(TAG, "Starting file download...")
            val outputStream = FileOutputStream(backupFile)
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.close()
            
            Log.d(TAG, "File download completed")
            Log.d(TAG, "Downloaded file exists: ${backupFile.exists()}")
            Log.d(TAG, "Downloaded file size: ${if (backupFile.exists()) backupFile.length() else 0} bytes")
            
            if (!backupFile.exists() || backupFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty or doesn't exist")
                return@withContext Result.failure(IOException("Downloaded backup file is empty"))
            }

            Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading backup", e)
            Log.e(TAG, "Download error - Type: ${e.javaClass.simpleName}, Message: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun restoreDatabase(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting database restore. File ID: $fileId")
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please sign in first.")
            )

            val databaseFile = context.getDatabasePath(DATABASE_NAME)
            val databaseDir = databaseFile.parentFile
            
            Log.d(TAG, "Database file path: ${databaseFile.absolutePath}")
            Log.d(TAG, "Database directory: ${databaseDir?.absolutePath}")
            Log.d(TAG, "Database directory exists: ${databaseDir?.exists()}")
            
            if (databaseDir == null) {
                Log.e(TAG, "Database directory is null")
                return@withContext Result.failure(IOException("Database directory is null"))
            }
            
            // Create directory if it doesn't exist (important after app data is cleared)
            if (!databaseDir.exists()) {
                Log.d(TAG, "Database directory doesn't exist, creating it...")
                val created = databaseDir.mkdirs()
                Log.d(TAG, "Database directory created: $created")
                if (!created || !databaseDir.exists()) {
                    Log.e(TAG, "Failed to create database directory")
                    return@withContext Result.failure(IOException("Failed to create database directory"))
                }
            }
            
            // Close and clear the database instance before restoring
            Log.d(TAG, "Closing database instance...")
            AppDatabase.closeInstance()
            kotlinx.coroutines.delay(200) // Give time for database to close
            
            // Download the backup to a temporary location
            Log.d(TAG, "Downloading backup from Drive...")
            val downloadResult = downloadBackup(fileId)
            val backupFilePath = downloadResult.getOrNull() 
                ?: run {
                    Log.e(TAG, "Failed to download backup")
                    return@withContext Result.failure(IOException("Failed to download backup"))
                }

            val backupFile = java.io.File(backupFilePath)
            Log.d(TAG, "Backup downloaded to: ${backupFile.absolutePath}")
            Log.d(TAG, "Backup file exists: ${backupFile.exists()}")
            Log.d(TAG, "Backup file size: ${if (backupFile.exists()) backupFile.length() else 0} bytes")
            
            if (!backupFile.exists()) {
                Log.e(TAG, "Downloaded backup file does not exist")
                return@withContext Result.failure(IOException("Downloaded backup file not found"))
            }
            
            // Create a backup of the current database before restoring
            val currentBackup = java.io.File(databaseDir, "${DATABASE_NAME}.pre_restore_backup")
            if (databaseFile.exists()) {
                Log.d(TAG, "Backing up current database to: ${currentBackup.absolutePath}")
                databaseFile.copyTo(currentBackup, overwrite = true)
                Log.d(TAG, "Current database backed up. Size: ${currentBackup.length()} bytes")
            } else {
                Log.d(TAG, "No existing database file to backup")
            }
            
            // Delete existing database files to ensure clean restore
            // Room uses WAL mode which creates -wal and -shm files
            val walFile = java.io.File(databaseDir, "${DATABASE_NAME}-wal")
            val shmFile = java.io.File(databaseDir, "${DATABASE_NAME}-shm")
            val journalFile = java.io.File(databaseDir, "${DATABASE_NAME}-journal")
            
            Log.d(TAG, "Cleaning up existing database files...")
            if (walFile.exists()) {
                Log.d(TAG, "Deleting WAL file: ${walFile.absolutePath} (size: ${walFile.length()} bytes)")
                walFile.delete()
                Log.d(TAG, "WAL file deleted: ${!walFile.exists()}")
            }
            if (shmFile.exists()) {
                Log.d(TAG, "Deleting SHM file: ${shmFile.absolutePath} (size: ${shmFile.length()} bytes)")
                shmFile.delete()
                Log.d(TAG, "SHM file deleted: ${!shmFile.exists()}")
            }
            if (journalFile.exists()) {
                Log.d(TAG, "Deleting journal file: ${journalFile.absolutePath} (size: ${journalFile.length()} bytes)")
                journalFile.delete()
                Log.d(TAG, "Journal file deleted: ${!journalFile.exists()}")
            }
            if (databaseFile.exists()) {
                Log.d(TAG, "Deleting existing database file (size: ${databaseFile.length()} bytes)")
                databaseFile.delete()
                Log.d(TAG, "Database file deleted: ${!databaseFile.exists()}")
            }
            
            // Copy the downloaded backup to replace the database
            Log.d(TAG, "Copying backup file to database location...")
            Log.d(TAG, "Source backup file size: ${backupFile.length()} bytes")
            backupFile.copyTo(databaseFile, overwrite = true)
            Log.d(TAG, "Backup file copied. New database file exists: ${databaseFile.exists()}")
            Log.d(TAG, "New database file size: ${if (databaseFile.exists()) databaseFile.length() else 0} bytes")
            
            // Verify the file was copied correctly
            if (!databaseFile.exists()) {
                Log.e(TAG, "ERROR: Database file was not created after restore!")
                return@withContext Result.failure(IOException("Database file was not created after restore"))
            }
            
            if (databaseFile.length() == 0L) {
                Log.e(TAG, "ERROR: Database file is empty after restore!")
                return@withContext Result.failure(IOException("Database file is empty after restore"))
            }
            
            if (databaseFile.length() != backupFile.length()) {
                Log.w(TAG, "WARNING: Database file size (${databaseFile.length()}) doesn't match backup size (${backupFile.length()})")
            } else {
                Log.d(TAG, "✓ File sizes match. Copy verified successfully.")
            }
            
            // Verify database integrity by trying to open it and count records
            try {
                Log.d(TAG, "Verifying restored database integrity...")
                val testDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                val version = testDb.version
                val tables = testDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                val tableCount = tables.count
                val tableNames = mutableListOf<String>()
                while (tables.moveToNext()) {
                    tableNames.add(tables.getString(0))
                }
                tables.close()
                testDb.close()
                
                Log.d(TAG, "✓ Database integrity verified!")
                Log.d(TAG, "  Database version: $version")
                Log.d(TAG, "  Number of tables: $tableCount")
                Log.d(TAG, "  Table names: $tableNames")
                
                // Count records in key tables
                val countDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                
                try {
                    val invCursor = countDb.rawQuery("SELECT COUNT(*) FROM inventory_items", null)
                    var invCount = 0
                    if (invCursor.moveToFirst()) {
                        invCount = invCursor.getInt(0)
                    }
                    invCursor.close()
                    Log.d(TAG, "  Inventory items count: $invCount")
                } catch (e: Exception) {
                    Log.w(TAG, "  Could not count inventory_items: ${e.message}")
                }
                
                try {
                    val capCursor = countDb.rawQuery("SELECT COUNT(*) FROM capital_transactions", null)
                    var capCount = 0
                    if (capCursor.moveToFirst()) {
                        capCount = capCursor.getInt(0)
                    }
                    capCursor.close()
                    Log.d(TAG, "  Capital transactions count: $capCount")
                } catch (e: Exception) {
                    Log.w(TAG, "  Could not count capital_transactions: ${e.message}")
                }
                
                try {
                    val outCursor = countDb.rawQuery("SELECT COUNT(*) FROM out_transactions", null)
                    var outCount = 0
                    if (outCursor.moveToFirst()) {
                        outCount = outCursor.getInt(0)
                    }
                    outCursor.close()
                    Log.d(TAG, "  Out transactions count: $outCount")
                } catch (e: Exception) {
                    Log.w(TAG, "  Could not count out_transactions: ${e.message}")
                }
                
                countDb.close()
                
            } catch (e: Exception) {
                Log.e(TAG, "ERROR: Could not verify database integrity: ${e.message}", e)
                // Continue anyway, let Room handle it
            }
            
            Log.d(TAG, "Database restore completed successfully!")
            Log.d(TAG, "Previous database backed up to: ${currentBackup.absolutePath}")
            Log.d(TAG, "New database file path: ${databaseFile.absolutePath}")
            Log.d(TAG, "New database file size: ${databaseFile.length()} bytes")
            
            Result.success("Database restored successfully. Please restart the app to see your data.")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring database", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteBackup(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting backup file. File ID: $fileId")
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please sign in first.")
            )

            // Get file metadata first to get the name for logging
            val fileMetadata = service.files().get(fileId).setFields("id, name").execute()
            Log.d(TAG, "Deleting backup: ${fileMetadata.name}")

            // Delete the file
            service.files().delete(fileId).execute()
            
            Log.d(TAG, "Backup deleted successfully: ${fileMetadata.name}")
            Result.success("Backup deleted successfully: ${fileMetadata.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup", e)
            Log.e(TAG, "Delete error - Type: ${e.javaClass.simpleName}, Message: ${e.message}")
            Result.failure(e)
        }
    }

    fun isInitialized(): Boolean = driveService != null
}

