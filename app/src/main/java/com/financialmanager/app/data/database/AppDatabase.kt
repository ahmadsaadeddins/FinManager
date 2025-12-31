package com.financialmanager.app.data.database

import androidx.room.Database
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
}

