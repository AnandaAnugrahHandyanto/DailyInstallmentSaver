package com.savares.dailyinstallmentsaver.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.util.CurrencyUtil
import com.savares.dailyinstallmentsaver.viewmodel.InstallmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: InstallmentViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    
    var installmentToDelete by remember { mutableStateOf<InstallmentEntity?>(null) }

    // FAB Visibility Logic - Optimized for max performance
    val isFabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || listState.firstVisibleItemScrollOffset < 150
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.dashboard_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    LanguageToggleButton(currentLanguage, onLanguageChange)
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToAdd()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 90.dp) // Clear of navbar
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_installment))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp, 
                bottom = 130.dp
            )
        ) {
            item(key = "total_card") {
                TotalSavingCard(uiState.totalDailySaving, uiState.walletBreakdown)
            }

            // Insights Section
            item(key = "insights_section") {
                InsightsSection(
                    currentStreak = uiState.currentStreak,
                    bestStreak = uiState.bestStreak,
                    daysSavedThisWeek = uiState.daysSavedThisWeek,
                    daysMissedThisWeek = uiState.daysMissedThisWeek,
                    overallProgress = uiState.overallProgress
                )
            }

            if (uiState.installments.isNotEmpty()) {
                item(key = "title_installments") {
                    Text(
                        text = "My Installments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(
                    items = uiState.installments,
                    key = { it.id }
                ) { installment ->
                    InstallmentItem(
                        installment = installment,
                        viewModel = viewModel,
                        onEdit = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToEdit(installment.id) 
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            installmentToDelete = installment
                        }
                    )
                }
            } else {
                item(key = "empty_state") {
                    EmptyState()
                }
            }
        }
    }

    if (installmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { installmentToDelete = null },
            title = { Text(stringResource(R.string.delete_installment)) },
            text = { Text(stringResource(R.string.delete_confirm_msg, installmentToDelete?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        installmentToDelete?.let { viewModel.deleteInstallment(it) }
                        installmentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    installmentToDelete = null 
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun InsightsSection(
    currentStreak: Int,
    bestStreak: Int,
    daysSavedThisWeek: Int,
    daysMissedThisWeek: Int,
    overallProgress: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.current_streak, currentStreak),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.best_streak, bestStreak),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.weekly_summary), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Text(stringResource(R.string.days_saved, daysSavedThisWeek), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.days_missed, daysMissedThisWeek), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.overall_progress, overallProgress), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun LanguageToggleButton(currentLanguage: String, onLanguageChange: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    TextButton(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val newLang = if (currentLanguage == "en") "in" else "en"
        onLanguageChange(newLang)
    }) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = if (currentLanguage == "en") " EN " else " ID ",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun TotalSavingCard(total: Double, breakdown: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val formattedTotal = remember(total) { CurrencyUtil.formatCurrency(total) }
            Text(
                text = stringResource(R.string.total_daily_saving),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Text(
                text = formattedTotal,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            if (breakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                
                breakdown.forEach { (wallet, amount) ->
                    key(wallet) {
                        val formattedAmount = remember(amount) { CurrencyUtil.formatCurrency(amount) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = wallet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = formattedAmount,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentItem(
    installment: InstallmentEntity,
    viewModel: InstallmentViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val daily = remember(installment.amount, installment.savedAmount, installment.dueDate) {
        viewModel.calculateDailySaving(installment)
    }
    val formattedDaily = remember(daily) { CurrencyUtil.formatCurrency(daily) }
    
    val daysLeft = remember(installment.dueDate) {
        viewModel.calculateDaysLeft(installment.dueDate)
    }
    val isSavedToday = remember(installment.lastSavedDate) {
        viewModel.isSavedToday(installment)
    }
    val progress by remember(installment.savedAmount, installment.amount) {
        derivedStateOf { (installment.savedAmount / installment.amount).coerceIn(0.0, 1.0).toFloat() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = installment.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = installment.wallet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit()
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            
            val formattedSaved = remember(installment.savedAmount) { CurrencyUtil.formatCurrency(installment.savedAmount) }
            val formattedTarget = remember(installment.amount) { CurrencyUtil.formatCurrency(installment.amount) }
            val progressPercent = remember(progress) { (progress * 100).toInt() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.progress,
                        formattedSaved,
                        formattedTarget,
                        progressPercent
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.days_left, daysLeft),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (daysLeft < 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optimized Glass Section: High Contrast, No real-time blur for 60fps
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.daily_needed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = formattedDaily,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Button(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.markAsSaved(installment) 
                        },
                        enabled = !isSavedToday,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedToday) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.9f),
                            disabledContentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isSavedToday) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.saved_today))
                            }
                        } else {
                            Text(stringResource(R.string.mark_as_saved))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.empty_list),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
