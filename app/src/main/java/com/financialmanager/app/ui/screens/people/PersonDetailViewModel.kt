package com.financialmanager.app.ui.screens.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.Currency
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.data.entities.PersonTransaction
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repository: PersonRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _person = MutableStateFlow<PersonAccount?>(null)
    val person: StateFlow<PersonAccount?> = _person.asStateFlow()

    // User preferences for currency
    val currency = userPreferences.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.EGP)
    
    fun setPersonId(id: Long) {
        viewModelScope.launch {
            _person.value = repository.getPersonById(id)
        }
    }

    fun getTransactions(personId: Long): Flow<List<PersonTransaction>> {
        return repository.getTransactionsByPerson(personId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getBalance(personId: Long): Flow<Double?> {
        return repository.getPersonBalance(personId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun insertTransaction(transaction: PersonTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: PersonTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: PersonTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}

