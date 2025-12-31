package com.financialmanager.app.di

import com.financialmanager.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideInventoryRepository(
        inventoryDao: com.financialmanager.app.data.dao.InventoryDao
    ): InventoryRepository {
        return InventoryRepository(inventoryDao)
    }

    @Provides
    @Singleton
    fun provideCapitalRepository(
        capitalDao: com.financialmanager.app.data.dao.CapitalDao
    ): CapitalRepository {
        return CapitalRepository(capitalDao)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: com.financialmanager.app.data.dao.TransactionDao
    ): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun providePersonRepository(
        personDao: com.financialmanager.app.data.dao.PersonDao
    ): PersonRepository {
        return PersonRepository(personDao)
    }
}

