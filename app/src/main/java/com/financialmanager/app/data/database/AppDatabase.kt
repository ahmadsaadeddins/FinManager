package com.financialmanager.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.financialmanager.app.data.dao.*
import com.financialmanager.app.data.entities.*
import com.financialmanager.app.data.database.MIGRATION_1_2
import com.financialmanager.app.data.database.MIGRATION_2_3
import com.financialmanager.app.data.database.MIGRATION_3_4
import com.financialmanager.app.data.database.MIGRATION_4_5
import com.financialmanager.app.data.database.MIGRATION_5_6

@Database(
    entities = [
        InventoryItem::class,
        CapitalTransaction::class,
        OutTransaction::class,
        PersonAccount::class,
        PersonTransaction::class,
        Balance::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun capitalDao(): CapitalDao
    abstract fun transactionDao(): TransactionDao
    abstract fun personDao(): PersonDao
    abstract fun transactionInventoryDao(): TransactionInventoryDao

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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
