package com.financialmanager.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.Currency
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(LocaleHelper.getSavedLanguage(context))
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(Currency.EGP)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.currency.collect { currency ->
                _selectedCurrency.value = currency
            }
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            LocaleHelper.saveLanguage(context, languageCode)
            _selectedLanguage.value = languageCode
        }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch {
            userPreferences.setCurrency(currency)
            _selectedCurrency.value = currency
        }
    }
}
