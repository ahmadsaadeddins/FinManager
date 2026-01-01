
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
