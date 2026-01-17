package com.financialmanager.app.ui.screens.people

import com.financialmanager.app.data.entities.PersonAccount

data class PersonWithBalance(
    val person: PersonAccount,
    val balance: Double
)
