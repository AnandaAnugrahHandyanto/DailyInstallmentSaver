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
import androidx.compose.material.icons.filled.TrendingUp
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
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.ui.theme.ColorSaved
import com.savares.dailyinstallmentsaver.ui.theme.GlassCard
import com.savares.dailyinstallmentsaver.ui.theme.GlassSurface
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
    var showAddSheet by remember { mutableStateOf(false) }

    var previousIndex by remember { mutableIntStateOf(0) }
    var previousOffset by remember { mutableIntStateOf(0) }
    var isFabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val scrolledDown = index > previousIndex ||
                    (index == previousIndex && offset > previousOffset)
                val scrolledUp = index < previousIndex ||
                    (index == previousIndex && offset < previousOffset)
                if (scrolledDown) isFabVisible = false
                else if (scrolledUp) isFabVisible = true
                previousIndex = index
                previousOffset = offset
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = fadeIn() + slideInVertically { fullHeight -> fullHeight },
                exit = fadeOut() + slideOutVertically { fullHeight -> fullHeight }
            ) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 90.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_installment))
                }
            }
        },
        containerColor = Color.Transparent
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
                        text = stringResource(R.string.my_installments),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(
                    items = uiState.installments,
                    key = { it.installment.id },
                    contentType = { "installment_item" }
                ) { itemState ->
                    InstallmentItem(
                        installment = itemState.installment,
                        onMarkAsSaved = { viewModel.markAsSaved(itemState.installment) },
                        onEdit = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToEdit(itemState.installment.id)
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            installmentToDelete = itemState.installment
                        },
                        periodSaving = itemState.periodSaving,
                        daysLeft = itemState.daysLeft,
                        isSavedToday = itemState.isSavedToday
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

    if (showAddSheet) {
        AddInstallmentBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
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
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        stringResource(R.string.weekly_summary),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.days_saved, daysSavedThisWeek),
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorSaved
                    )
                    Text(
                        stringResource(R.string.days_missed, daysMissedThisWeek),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.overall_progress, overallProgress),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val formattedTotal = remember(total) { CurrencyUtil.formatCurrency(total) }
            Text(
                text = stringResource(R.string.total_daily_saving),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(4.dp))
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
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
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
    onMarkAsSaved: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    periodSaving: Double,
    daysLeft: Long,
    isSavedToday: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val formattedPeriodSaving = remember(periodSaving) { CurrencyUtil.formatCurrency(periodSaving) }
    val savingLabel = if (installment.savingType == "WEEKLY") {
        stringResource(R.string.weekly_saving)
    } else {
        stringResource(R.string.daily_saving)
    }

    val progress by remember(installment.savedAmount, installment.collectedAmount, installment.amount) {
        derivedStateOf { ((installment.savedAmount + installment.collectedAmount) / installment.amount).coerceIn(0.0, 1.0).toFloat() }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp
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
                    .height(8.dp)
                    .clip(CircleShape),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )

            val formattedSaved = remember(installment.savedAmount, installment.collectedAmount) { CurrencyUtil.formatCurrency(installment.savedAmount + installment.collectedAmount) }
            val formattedTarget = remember(installment.amount) { CurrencyUtil.formatCurrency(installment.amount) }
            val progressPercent = remember(progress) { (progress * 100).toInt() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.progress, formattedSaved, formattedTarget, progressPercent),
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

            Spacer(modifier = Modifier.height(14.dp))

            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
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
                            text = savingLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = formattedPeriodSaving,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMarkAsSaved()
                        },
                        enabled = !isSavedToday,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedToday) ColorSaved else MaterialTheme.colorScheme.primary,
                            disabledContainerColor = ColorSaved.copy(alpha = 0.9f),
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
