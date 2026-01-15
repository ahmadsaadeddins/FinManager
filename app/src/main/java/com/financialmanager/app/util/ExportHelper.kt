package com.financialmanager.app.util

import android.content.Context
import android.os.Environment
import com.financialmanager.app.data.entities.*
import com.financialmanager.app.data.repository.*
import kotlinx.coroutines.flow.first
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExportHelper(
    private val context: Context,
    private val inventoryRepository: InventoryRepository,
    private val capitalRepository: CapitalRepository,
    private val transactionRepository: TransactionRepository,
    private val personRepository: PersonRepository
) {
    suspend fun exportToExcel(): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            
            // Inventory Sheet
            val inventorySheet = workbook.createSheet("Inventory")
            val inventoryItems = inventoryRepository.getAllItems().first()
            createInventorySheet(inventorySheet, inventoryItems)
            
            // Capital Sheet
            val capitalSheet = workbook.createSheet("Capital")
            val capitalTransactions = capitalRepository.getAllTransactions().first()
            createCapitalSheet(capitalSheet, capitalTransactions)
            
            // Transactions Sheet
            val transactionsSheet = workbook.createSheet("Transactions")
            val transactions = transactionRepository.getAllTransactions().first()
            createTransactionsSheet(transactionsSheet, transactions)
            
            // People Sheet
            val peopleSheet = workbook.createSheet("People")
            val people = personRepository.getAllPeople().first()
            createPeopleSheet(peopleSheet, people)
            
            // Person Transactions Sheets
            people.forEach { person ->
                val personTransactions = personRepository.getTransactionsByPerson(person.id).first()
                if (personTransactions.isNotEmpty()) {
                    val personSheet = workbook.createSheet(person.name.take(31)) // Excel sheet name limit
                    createPersonTransactionsSheet(personSheet, person, personTransactions)
                }
            }
            
            // Save file
            val fileName = "FinancialManager_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xlsx"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createInventorySheet(sheet: org.apache.poi.ss.usermodel.Sheet, items: List<InventoryItem>) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Category")
        headerRow.createCell(2).setCellValue("Quantity")
        headerRow.createCell(3).setCellValue("Purchase Price")
        headerRow.createCell(4).setCellValue("Selling Price")
        headerRow.createCell(5).setCellValue("Wholesale Price")
        headerRow.createCell(6).setCellValue("Notes")
        
        items.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(item.name)
            row.createCell(1).setCellValue(item.category ?: "")
            row.createCell(2).setCellValue(item.quantity.toDouble())
            row.createCell(3).setCellValue(item.purchasePrice)
            row.createCell(4).setCellValue(item.sellingPrice)
            row.createCell(5).setCellValue(item.wholesalePrice)
            row.createCell(6).setCellValue(item.notes ?: "")
        }
    }
    
    private fun createCapitalSheet(sheet: org.apache.poi.ss.usermodel.Sheet, transactions: List<CapitalTransaction>) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Date")
        headerRow.createCell(1).setCellValue("Source")
        headerRow.createCell(2).setCellValue("Amount")
        headerRow.createCell(3).setCellValue("Type")
        headerRow.createCell(4).setCellValue("Description")
        headerRow.createCell(5).setCellValue("Notes")
        
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.date)))
            row.createCell(1).setCellValue(transaction.source)
            row.createCell(2).setCellValue(transaction.amount)
            row.createCell(3).setCellValue(transaction.type.value)
            row.createCell(4).setCellValue(transaction.description ?: "")
            row.createCell(5).setCellValue(transaction.notes ?: "")
        }
    }
    
    private fun createTransactionsSheet(sheet: org.apache.poi.ss.usermodel.Sheet, transactions: List<OutTransaction>) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Date")
        headerRow.createCell(1).setCellValue("Type")
        headerRow.createCell(2).setCellValue("Category")
        headerRow.createCell(3).setCellValue("Amount")
        headerRow.createCell(4).setCellValue("Description")
        headerRow.createCell(5).setCellValue("Notes")
        
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.date)))
            row.createCell(1).setCellValue(transaction.type.name)
            row.createCell(2).setCellValue(transaction.category ?: "")
            row.createCell(3).setCellValue(transaction.amount)
            row.createCell(4).setCellValue(transaction.description ?: "")
            row.createCell(5).setCellValue(transaction.notes ?: "")
        }
    }
    
    private fun createPeopleSheet(sheet: org.apache.poi.ss.usermodel.Sheet, people: List<PersonAccount>) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Phone")
        headerRow.createCell(2).setCellValue("Email")
        
        people.forEachIndexed { index, person ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(person.name)
            row.createCell(1).setCellValue(person.phone ?: "")
            row.createCell(2).setCellValue(person.email ?: "")
        }
    }
    
    private fun createPersonTransactionsSheet(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        person: PersonAccount,
        transactions: List<PersonTransaction>
    ) {
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Date")
        headerRow.createCell(1).setCellValue("Type")
        headerRow.createCell(2).setCellValue("Amount")
        headerRow.createCell(3).setCellValue("Category")
        headerRow.createCell(4).setCellValue("Description")
        headerRow.createCell(5).setCellValue("Notes")
        
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.date)))
            row.createCell(1).setCellValue(transaction.type.value)
            row.createCell(2).setCellValue(transaction.amount)
            row.createCell(3).setCellValue(transaction.category ?: "")
            row.createCell(4).setCellValue(transaction.description ?: "")
            row.createCell(5).setCellValue(transaction.notes ?: "")
        }
    }
}

