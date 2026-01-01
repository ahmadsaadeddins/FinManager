package com.financialmanager.app.di

import android.content.Context
import com.financialmanager.app.service.GoogleDriveBackupService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideGoogleDriveBackupService(
        @ApplicationContext context: Context
    ): GoogleDriveBackupService {
        return GoogleDriveBackupService(context)
    }
}




