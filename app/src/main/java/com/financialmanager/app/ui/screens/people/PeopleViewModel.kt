package com.financialmanager.app.ui.screens.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.Currency
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.data.preferences.UserPreferences
import com.financialmanager.app.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: PersonRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // User preferences for currency
    val currency = userPreferences.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.EGP)

    val people = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllPeople()
        } else {
            repository.searchPeople(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peopleWithBalances: StateFlow<List<PersonWithBalance>> = people.flatMapLatest { peopleList ->
        if (peopleList.isEmpty()) {
            flowOf(emptyList<PersonWithBalance>())
        } else {
            val flows = peopleList.map { person ->
                repository.getPersonBalance(person.id).map { balance ->
                    PersonWithBalance(person, balance ?: 0.0)
                }
            }
            combine(flows) { balances -> 
                balances.sortedWith(
                    compareByDescending<PersonWithBalance> { it.balance != 0.0 }
                        .thenByDescending { it.balance >= 0 }
                        .thenByDescending { it.person.usageCount }
                        .thenByDescending { it.person.lastUsedAt }
                        .thenByDescending { it.balance }
                        .thenBy { it.person.name.lowercase() }
                ).toList()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peopleCount: StateFlow<Int> = peopleWithBalances.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val positiveBalanceCount: StateFlow<Int> = peopleWithBalances.map { list ->
        list.count { it.balance >= 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertPerson(person: PersonAccount) {
        viewModelScope.launch {
            repository.insertPerson(person)
        }
    }

    fun updatePerson(person: PersonAccount) {
        viewModelScope.launch {
            repository.updatePerson(person)
        }
    }

    fun deletePerson(person: PersonAccount) {
        viewModelScope.launch {
            repository.deletePerson(person)
        }
    }

    fun trackPersonUsage(personId: Long) {
        viewModelScope.launch {
            repository.incrementPersonUsage(personId)
        }
    }

    fun transferBetweenPeople(
        fromPersonId: Long,
        toPersonId: Long,
        amount: Double,
        description: String = "Transfer"
    ) {
        viewModelScope.launch {
            repository.transferBetweenPeople(fromPersonId, toPersonId, amount, description)
        }
    }
}
