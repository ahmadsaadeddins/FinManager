package com.financialmanager.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.financialmanager.app.data.dao.*
import com.financialmanager.app.data.entities.*

@Database(
    entities = [
        InventoryItem::class,
        CapitalTransaction::class,
        OutTransaction::class,
        PersonAccount::class,
        PersonTransaction::class,
        Balance::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun capitalDao(): CapitalDao
    abstract fun transactionDao(): TransactionDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }

        @Synchronized
        fun closeInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
