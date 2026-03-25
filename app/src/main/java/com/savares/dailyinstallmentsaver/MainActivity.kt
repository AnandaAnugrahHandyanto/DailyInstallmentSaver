package com.savares.dailyinstallmentsaver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import com.savares.dailyinstallmentsaver.ui.SettingsScreen
import com.savares.dailyinstallmentsaver.ui.StatsScreen
import com.savares.dailyinstallmentsaver.ui.theme.DailyInstallmentSaverTheme
import com.savares.dailyinstallmentsaver.util.LanguageConfig
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var languageCode by remember { mutableStateOf(LanguageConfig.getLanguage(this)) }
            val context = LocalContext.current
            
            // Notification Permission Request
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

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
    object Settings : Screen("settings", R.string.settings_title, Icons.Default.Settings)
}

@Composable
fun DailyInstallmentApp(currentLanguage: String, onLanguageChange: (String) -> Unit) {
    val navController = rememberNavController()
    val viewModel: InstallmentViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val haptic = LocalHapticFeedback.current

    val items = remember {
        listOf(
            Screen.Dashboard,
            Screen.History,
            Screen.Stats,
            Screen.Settings
        )
    }

    val showBottomBar = remember(currentDestination) {
        currentDestination?.route in items.map { it.route }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController, 
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                route = Screen.Dashboard.route,
                enterTransition = { fadeIn(tween(100)) },
                exitTransition = { fadeOut(tween(100)) }
            ) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navController.navigate("add_installment") },
                    onNavigateToEdit = { id -> navController.navigate("edit_installment/$id") },
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
            composable(
                route = Screen.History.route,
                enterTransition = { fadeIn(tween(100)) },
                exitTransition = { fadeOut(tween(100)) }
            ) {
                HistoryScreen(viewModel = viewModel)
            }
            composable(
                route = Screen.Stats.route,
                enterTransition = { fadeIn(tween(100)) },
                exitTransition = { fadeOut(tween(100)) }
            ) {
                StatsScreen(viewModel = viewModel)
            }
            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(tween(100)) },
                exitTransition = { fadeOut(tween(100)) }
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
            composable(
                route = "add_installment",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) }
            ) {
                AddInstallmentScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_installment/{installmentId}",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("installmentId")?.toIntOrNull()
                AddInstallmentScreen(
                    viewModel = viewModel,
                    installmentId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Floating Bottom Bar Overlay
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() 
                    .padding(bottom = 12.dp, start = 32.dp, end = 32.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Box(modifier = Modifier.fillMaxSize().blur(10.dp))
                    }
                    
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        screen.icon, 
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        stringResource(screen.resourceId), 
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                    ) 
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!selected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
