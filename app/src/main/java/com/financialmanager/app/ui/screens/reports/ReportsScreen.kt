package com.financialmanager.app.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.financialmanager.app.R
import com.financialmanager.app.ui.components.BottomNavigationBar
import com.financialmanager.app.ui.navigation.Screen
import com.financialmanager.app.ui.theme.*
import com.financialmanager.app.util.Formatters
import com.financialmanager.app.util.LocaleHelper
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
    val totalCOGS by viewModel.totalCOGS.collectAsState()
    val generalProfit by viewModel.generalProfit.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isRTL = LocaleHelper.isRTL()

    val profitLoss = (totalSales ?: 0.0) - (totalCOGS ?: 0.0) - (totalExpenses ?: 0.0)
    val netCapital = (totalInvestments ?: 0.0) - (totalWithdrawals ?: 0.0)

    val reportItems = listOf(
        ReportItem(stringResource(R.string.total_capital), totalCapital, MoneyIn, Icons.Default.AccountBalance),
        ReportItem(stringResource(R.string.net_capital), netCapital, if (netCapital >= 0) MoneyIn else MoneyOut, Icons.AutoMirrored.Filled.TrendingUp),
        ReportItem(stringResource(R.string.total_investments), totalInvestments, MoneyIn, Icons.Default.Add),
        ReportItem(stringResource(R.string.total_withdrawals), totalWithdrawals, MoneyOut, Icons.Default.Remove),
        ReportItem(stringResource(R.string.inventory_value), totalInventoryValue, Inventory, Icons.Default.Inventory),
        ReportItem(stringResource(R.string.total_sales), totalSales, MoneyIn, Icons.AutoMirrored.Filled.TrendingUp),
        ReportItem(stringResource(R.string.total_expenses), totalExpenses, MoneyOut, Icons.AutoMirrored.Filled.TrendingDown),
        ReportItem(stringResource(R.string.profit_loss), profitLoss, if (profitLoss >= 0) MoneyIn else MoneyOut, Icons.Default.Assessment),
        ReportItem(stringResource(R.string.general_balance), generalProfit, if (generalProfit >= 0) MoneyIn else MoneyOut, Icons.Default.Balance)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export))
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
                            text = stringResource(R.string.recent_operations),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.recent_operations_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { navController.navigate(Screen.RecentOperations.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.view_recent_operations))
                        }
                    }
                }
            }
            
            items(reportItems) { item ->
                ReportCard(item = item, currencySymbol = currency.symbol, isRTL = isRTL)
            }
        }
    }
    
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.export_data)) },
            text = { Text(stringResource(R.string.export_placeholder_info)) },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
fun ReportCard(item: ReportItem, currencySymbol: String, isRTL: Boolean) {
    val valueText = Formatters.formatCurrency(item.value, currencySymbol, isRTL)

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

