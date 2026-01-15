package com.financialmanager.app.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.*
import java.text.NumberFormat
import java.util.*

data class ReportItem(
    val title: String,
    val value: Double?,
    val color: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    var showExportDialog by remember { mutableStateOf(false) }
    val totalCapital by viewModel.totalCapital.collectAsState()
    val totalInventoryValue by viewModel.totalInventoryValue.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val totalInvestments by viewModel.totalInvestments.collectAsState()
    val totalWithdrawals by viewModel.totalWithdrawals.collectAsState()

    val profitLoss = (totalSales ?: 0.0) - (totalExpenses ?: 0.0)
    val netCapital = (totalInvestments ?: 0.0) - (totalWithdrawals ?: 0.0)

    val reportItems = listOf(
        ReportItem("Total Capital", totalCapital, MoneyIn, Icons.Default.AccountBalance),
        ReportItem("Net Capital", netCapital, if (netCapital >= 0) MoneyIn else MoneyOut, Icons.Default.TrendingUp),
        ReportItem("Total Investments", totalInvestments, MoneyIn, Icons.Default.Add),
        ReportItem("Total Withdrawals", totalWithdrawals, MoneyOut, Icons.Default.Remove),
        ReportItem("Inventory Value", totalInventoryValue, Inventory, Icons.Default.Inventory),
        ReportItem("Total Sales", totalSales, MoneyIn, Icons.Default.TrendingUp),
        ReportItem("Total Expenses", totalExpenses, MoneyOut, Icons.Default.TrendingDown),
        ReportItem("Profit/Loss", profitLoss, if (profitLoss >= 0) MoneyIn else MoneyOut, Icons.Default.Assessment)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Reports.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recent Operations button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Recent Operations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View, edit, and delete recent transactions and operations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { navController.navigate(Screen.RecentOperations.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View Recent Operations")
                        }
                    }
                }
            }
            
            items(reportItems) { item ->
                ReportCard(item = item)
            }
        }
    }
    
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { Text("Export functionality will be available after proper repository injection is set up.") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ReportCard(item: ReportItem) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val valueText = item.value?.let { formatter.format(it) } ?: "$0.00"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = item.color
            )
        }
    }
}

