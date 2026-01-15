
package com.financialmanager.app.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration: Add a new column to a table
        database.execSQL("ALTER TABLE 'inventory_item' ADD COLUMN 'new_column' INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration: Add a new table
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
