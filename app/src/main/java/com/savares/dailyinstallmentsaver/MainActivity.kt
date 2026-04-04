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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.savares.dailyinstallmentsaver.ui.*
import com.savares.dailyinstallmentsaver.ui.theme.DailyInstallmentSaverTheme
import com.savares.dailyinstallmentsaver.ui.theme.GlassAlpha
import com.savares.dailyinstallmentsaver.util.LanguageConfig
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var languageCode by remember { mutableStateOf(LanguageConfig.getLanguage(this)) }
            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Result is not used; notification permission is optional */ }

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
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
    object Stats : Screen("stats", R.string.stats_title, Icons.Default.BarChart)
    object History : Screen("history", R.string.history_title, Icons.Default.History)
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
        listOf(Screen.Dashboard, Screen.Stats, Screen.History, Screen.Settings)
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
            composable(Screen.Dashboard.route,
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(220)) }
            ) {
                DashboardScreen(viewModel, { navController.navigate("add_installment") }, { id -> navController.navigate("edit_installment/$id") }, currentLanguage, onLanguageChange)
            }
            composable(Screen.Stats.route,
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(220)) }
            ) { StatsScreen(viewModel) }
            composable(Screen.History.route,
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(220)) }
            ) { HistoryScreen(viewModel) }
            composable(Screen.Settings.route,
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(220)) }
            ) { SettingsScreen(viewModel, currentLanguage, onLanguageChange) }

            composable("add_installment",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(280)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(280)) }
            ) { AddInstallmentScreen(viewModel, onNavigateBack = { navController.popBackStack() }) }

            composable("edit_installment/{installmentId}",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(280)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(280)) }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("installmentId")?.toIntOrNull()
                AddInstallmentScreen(viewModel, id, onNavigateBack = { navController.popBackStack() })
            }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(tween(280)) { it } + fadeIn(tween(280)),
            exit = slideOutVertically(tween(280)) { it } + fadeOut(tween(280)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 20.dp, end = 20.dp)
        ) {
            GlassBottomNavBar(
                items = items,
                currentDestination = currentDestination,
                onItemSelected = { screen ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun GlassBottomNavBar(
    items: List<Screen>,
    currentDestination: androidx.navigation.NavDestination?,
    onItemSelected: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = GlassAlpha.NAV_BAR),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.08f)
        ),
        shadowElevation = 24.dp,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavBarItem(
                    screen = screen,
                    selected = selected,
                    onClick = { if (!selected) onItemSelected(screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.5f,
        animationSpec = tween(durationMillis = 220),
        label = "icon_alpha"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 32.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "indicator_width"
    )
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = iconAlpha)
        )
        Text(
            text = stringResource(screen.resourceId),
            fontSize = 10.sp,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold
            else androidx.compose.ui.text.font.FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = iconAlpha),
            maxLines = 1
        )
    }
}

