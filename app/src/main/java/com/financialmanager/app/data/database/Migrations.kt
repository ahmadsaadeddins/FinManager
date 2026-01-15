
package com.financialmanager.app.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Fix table name from 'inventory_item' to 'inventory_items'
        // Check if table exists before adding column
        database.execSQL("ALTER TABLE 'inventory_items' ADD COLUMN 'new_column' INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration: Add a new table if it doesn't exist
        database.execSQL("CREATE TABLE IF NOT EXISTS `new_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add usage tracking fields to person_accounts table
        database.execSQL("ALTER TABLE person_accounts ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE person_accounts ADD COLUMN lastUsedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add archive fields to out_transactions table for sales archive feature
        database.execSQL("ALTER TABLE out_transactions ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE out_transactions ADD COLUMN archivedAt INTEGER")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add missing index on capital_transactions.date column
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_capital_transactions_date` ON `capital_transactions` (`date`)")
        
        // Add missing indexes on out_transactions table
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_out_transactions_date` ON `out_transactions` (`date`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_out_transactions_type` ON `out_transactions` (`type`)")
        
        // Add missing indexes on person_transactions table
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_person_transactions_personId` ON `person_transactions` (`personId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_person_transactions_date` ON `person_transactions` (`date`)")
    }
}
