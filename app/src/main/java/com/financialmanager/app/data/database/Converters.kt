package com.financialmanager.app.data.database

import androidx.room.TypeConverter
import com.financialmanager.app.data.entities.TransactionType
import com.financialmanager.app.data.entities.PersonTransactionType
import com.financialmanager.app.data.entities.CapitalTransactionType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.entries.find { it.value == value } }
    }

    @TypeConverter
    fun transactionTypeToString(type: TransactionType?): String? {
        return type?.value
    }

    @TypeConverter
    fun fromPersonTransactionType(value: String?): PersonTransactionType? {
        return value?.let { PersonTransactionType.entries.find { it.value == value } }
    }

    @TypeConverter
    fun personTransactionTypeToString(type: PersonTransactionType?): String? {
        return type?.value
    }

    @TypeConverter
    fun fromCapitalTransactionType(value: String?): CapitalTransactionType? {
        return value?.let { CapitalTransactionType.entries.find { it.value == value } }
    }

    @TypeConverter
    fun capitalTransactionTypeToString(type: CapitalTransactionType?): String? {
        return type?.value
    }
}

