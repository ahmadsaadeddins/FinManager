package com.financialmanager.app.ui.screens.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.financialmanager.app.data.entities.PersonAccount
import com.financialmanager.app.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: PersonRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val people = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllPeople()
        } else {
            repository.searchPeople(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}

