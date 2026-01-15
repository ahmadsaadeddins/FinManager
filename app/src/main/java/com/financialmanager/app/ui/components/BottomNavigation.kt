package com.financialmanager.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.financialmanager.app.R
import com.financialmanager.app.ui.navigation.Screen

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(stringResource(R.string.home), Icons.Default.Home, Screen.Home.route),
        BottomNavItem(stringResource(R.string.inventory), Icons.Default.Inventory, Screen.Inventory.route),
        BottomNavItem(stringResource(R.string.capital), Icons.Default.AccountBalance, Screen.Capital.route),
        BottomNavItem(stringResource(R.string.transactions), Icons.Default.Receipt, Screen.Transactions.route),
        BottomNavItem(stringResource(R.string.people), Icons.Default.People, Screen.People.route)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

