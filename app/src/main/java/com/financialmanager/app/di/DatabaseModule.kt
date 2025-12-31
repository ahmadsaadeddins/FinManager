package com.financialmanager.app.di

import android.content.Context
import androidx.room.Room
import com.financialmanager.app.data.dao.*
import com.financialmanager.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "financial_manager_database"
        )
        .fallbackToDestructiveMigration() // For development - removes data on schema change
        .build()
    }

    @Provides
    fun provideInventoryDao(database: AppDatabase): InventoryDao = database.inventoryDao()

    @Provides
    fun provideCapitalDao(database: AppDatabase): CapitalDao = database.capitalDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun providePersonDao(database: AppDatabase): PersonDao = database.personDao()
}

