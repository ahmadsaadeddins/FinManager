package com.financialmanager.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.financialmanager.app.ui.screens.backup.BackupScreen
import com.financialmanager.app.ui.screens.capital.CapitalScreen
import com.financialmanager.app.ui.screens.home.HomeScreen
import com.financialmanager.app.ui.screens.inventory.InventoryScreen
import com.financialmanager.app.ui.screens.people.PeopleScreen
import com.financialmanager.app.ui.screens.people.PersonDetailScreen
import com.financialmanager.app.ui.screens.reports.ReportsScreen
import com.financialmanager.app.ui.screens.reports.RecentOperationsScreen
import com.financialmanager.app.ui.screens.archive.SalesArchiveScreen
import com.financialmanager.app.ui.screens.search.SearchScreen
import com.financialmanager.app.ui.screens.settings.SettingsScreen
import com.financialmanager.app.ui.screens.transactions.TransactionScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Inventory : Screen("inventory")
    object Capital : Screen("capital")
    object Transactions : Screen("transactions")
    object People : Screen("people")
    object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: Long) = "person_detail/$personId"
    }
    object Reports : Screen("reports")
    object RecentOperations : Screen("recent_operations")
    object SalesArchive : Screen("sales_archive")
    object Search : Screen("search")
    object Backup : Screen("backup")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(navController = navController)
        }
        composable(Screen.Capital.route) {
            CapitalScreen(navController = navController)
        }
        composable(Screen.Transactions.route) {
            TransactionScreen(navController = navController)
        }
        composable(Screen.People.route) {
            PeopleScreen(navController = navController)
        }
        composable(Screen.PersonDetail.route) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull() ?: 0L
            PersonDetailScreen(
                navController = navController,
                personId = personId
            )
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.RecentOperations.route) {
            RecentOperationsScreen(navController = navController)
        }
        composable(Screen.SalesArchive.route) {
            SalesArchiveScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Backup.route) {
            BackupScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}


