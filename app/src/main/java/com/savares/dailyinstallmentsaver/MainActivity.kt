package com.savares.dailyinstallmentsaver

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.savares.dailyinstallmentsaver.ui.AddInstallmentScreen
import com.savares.dailyinstallmentsaver.ui.DashboardScreen
import com.savares.dailyinstallmentsaver.ui.HistoryScreen
import com.savares.dailyinstallmentsaver.ui.StatsScreen
import com.savares.dailyinstallmentsaver.ui.theme.DailyInstallmentSaverTheme
import com.savares.dailyinstallmentsaver.util.LanguageConfig
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var languageCode by remember { mutableStateOf(LanguageConfig.getLanguage(this)) }
            
            DailyInstallmentSaverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DailyInstallmentApp(
                        currentLanguage = languageCode,
                        onLanguageChange = { newLang ->
                            LanguageConfig.setLanguage(this, newLang)
                            languageCode = newLang
                            recreate()
                        }
                    )
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = LanguageConfig.getLanguage(newBase)
        super.attachBaseContext(LanguageConfig.updateResources(newBase, lang))
    }
}

sealed class Screen(val route: String, val resourceId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", R.string.dashboard_title, Icons.Default.Dashboard)
    object History : Screen("history", R.string.history_title, Icons.Default.History)
    object Stats : Screen("stats", R.string.stats_title, Icons.Default.BarChart)
}

@Composable
fun DailyInstallmentApp(currentLanguage: String, onLanguageChange: (String) -> Unit) {
    val navController = rememberNavController()
    val viewModel: InstallmentViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Dashboard,
        Screen.History,
        Screen.Stats
    )

    val showBottomBar = currentDestination?.route in items.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navController.navigate("add_installment") },
                    onNavigateToEdit = { id -> navController.navigate("edit_installment/$id") },
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable("add_installment") {
                AddInstallmentScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("edit_installment/{installmentId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("installmentId")?.toIntOrNull()
                AddInstallmentScreen(
                    viewModel = viewModel,
                    installmentId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
