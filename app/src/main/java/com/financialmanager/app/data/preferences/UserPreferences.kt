package com.financialmanager.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.financialmanager.app.data.entities.Currency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Use a top-level extension on Context that always resolves to applicationContext
// This prevents issues when locale changes create wrapped configuration contexts
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext appContext: Context
) {
    // Always use the applicationContext to avoid issues with wrapped configuration contexts
    // that are created when locale settings change (e.g., switching to Arabic)
    private val context: Context = appContext.applicationContext
    private object PreferencesKeys {
        val HIDE_NUMBERS = booleanPreferencesKey("hide_numbers")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val GOOGLE_ACCOUNT_NAME = stringPreferencesKey("google_account_name")
        val CURRENCY = stringPreferencesKey("currency")
    }

    val hideNumbers: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HIDE_NUMBERS] ?: false
        }

    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP_ENABLED] ?: true // Default to enabled
        }

    val googleAccountName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GOOGLE_ACCOUNT_NAME]
        }

    val currency: Flow<Currency> = context.dataStore.data
        .map { preferences ->
            val code = preferences[PreferencesKeys.CURRENCY] ?: Currency.EGP.code
            Currency.fromCode(code)
        }

    suspend fun setHideNumbers(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_NUMBERS] = hide
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setGoogleAccountName(accountName: String?) {
        context.dataStore.edit { preferences ->
            if (accountName != null) {
                preferences[PreferencesKeys.GOOGLE_ACCOUNT_NAME] = accountName
            } else {
                preferences.remove(PreferencesKeys.GOOGLE_ACCOUNT_NAME)
            }
        }
    }

    suspend fun setCurrency(currency: Currency) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY] = currency.code
        }
    }
}