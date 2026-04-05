package com.savares.dailyinstallmentsaver.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.data.InstallmentDao
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DashboardInstallmentUiState(
    val installment: InstallmentEntity,
    val periodSaving: Double,
    val daysLeft: Long,
    val isSavedToday: Boolean,
    val progress: Float
)

data class DashboardUiState(
    val installments: List<DashboardInstallmentUiState> = emptyList(),
    val totalDailySaving: Double = 0.0,
    val walletBreakdown: Map<String, Double> = emptyMap(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val daysSavedThisWeek: Int = 0,
    val daysMissedThisWeek: Int = 0,
    val overallProgress: Int = 0
)

data class StatsUiState(
    val logsByDate: Map<String, List<SavingLogEntity>> = emptyMap(),
    val trendPoints: List<Float> = emptyList()
)

class InstallmentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).installmentDao()

    fun getDao(): InstallmentDao = dao

    val installments: StateFlow<List<InstallmentEntity>> = dao.getAll()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingLogs: StateFlow<List<SavingLogEntity>> = dao.getAllLogs()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDailySaving: StateFlow<Double> = installments.map { list ->
        list.sumOf { calculateDailySaving(it) }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val walletBreakdown: StateFlow<Map<String, Double>> = installments.map { list ->
        list.groupBy { it.wallet }
            .mapValues { entry -> entry.value.sumOf { calculateDailySaving(it) } }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        installments, totalDailySaving, walletBreakdown, savingLogs
    ) { inst, total, wallet, logs ->
        val streakInfo = calculateStreaks(logs)
        val weeklyInfo = calculateWeeklySummary(logs)
        
        val installmentUiStates = inst.map { 
            DashboardInstallmentUiState(
                installment = it,
                periodSaving = calculatePeriodSaving(it),
                daysLeft = calculateDaysLeft(it.dueDate),
                isSavedToday = isSavedToday(it),
                progress = if (it.amount > 0) ((it.savedAmount + it.collectedAmount) / it.amount).coerceIn(0.0, 1.0).toFloat() else 0f
            )
        }

        val progress = if (inst.isEmpty()) 0 else {
            val totalTarget = inst.sumOf { it.amount }
            if (totalTarget > 0) {
                (inst.sumOf { it.savedAmount + it.collectedAmount } / totalTarget * 100).toInt().coerceIn(0, 100)
            } else 0
        }
        
        DashboardUiState(
            installments = installmentUiStates,
            totalDailySaving = total,
            walletBreakdown = wallet,
            currentStreak = streakInfo.first,
            bestStreak = streakInfo.second,
            daysSavedThisWeek = weeklyInfo.first,
            daysMissedThisWeek = weeklyInfo.second,
            overallProgress = progress
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    val statsUiState: StateFlow<StatsUiState> = savingLogs.map { logs ->
        val logsByDate = logs.groupBy { log ->
            val cal = Calendar.getInstance().apply { timeInMillis = log.date }
            getDateKey(cal)
        }
        StatsUiState(
            logsByDate = logsByDate,
            trendPoints = calculateTrendPoints(logsByDate)
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    private fun calculateTrendPoints(logs: Map<String, List<SavingLogEntity>>): List<Float> {
        val today = Calendar.getInstance()
        val last7Days = (0 until 7).map { i ->
            val cal = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = getDateKey(cal)
            logs[key]?.sumOf { it.amount } ?: 0.0
        }.reversed()
        
        val max = last7Days.maxOrNull() ?: 1.0
        val finalMax = if (max == 0.0) 1.0 else max
        return last7Days.map { (it / finalMax).toFloat() }
    }

    private fun calculateStreaks(logs: List<SavingLogEntity>): Pair<Int, Int> {
        if (logs.isEmpty()) return Pair(0, 0)
        
        val dates = logs.map { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        if (dates.isEmpty()) return Pair(0, 0)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - TimeUnit.DAYS.toMillis(1)

        var currentStreak = 0
        var bestStreak = 0
        
        // Calculate current streak
        if (dates.first() == today || dates.first() == yesterday) {
            var lastDate = dates.first()
            currentStreak = 1
            for (i in 1 until dates.size) {
                if (dates[i] == lastDate - TimeUnit.DAYS.toMillis(1)) {
                    currentStreak++
                    lastDate = dates[i]
                } else {
                    break
                }
            }
        }

        // Calculate best streak
        if (dates.isNotEmpty()) {
            var tempStreak = 1
            bestStreak = 1
            for (i in 1 until dates.size) {
                if (dates[i] == dates[i-1] - TimeUnit.DAYS.toMillis(1)) {
                    tempStreak++
                } else {
                    bestStreak = maxOf(bestStreak, tempStreak)
                    tempStreak = 1
                }
            }
            bestStreak = maxOf(bestStreak, tempStreak)
        }

        return Pair(currentStreak, bestStreak)
    }

    private fun calculateWeeklySummary(logs: List<SavingLogEntity>): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfWeek = calendar.timeInMillis
        
        val logsThisWeek = logs.filter { it.date >= startOfWeek }
        val daysWithSavings = logsThisWeek.map { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            getDateKey(cal)
        }.distinct().size

        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val daysPassedThisWeek = if (dayOfWeek >= calendar.firstDayOfWeek) {
            dayOfWeek - calendar.firstDayOfWeek + 1
        } else {
            7 - (calendar.firstDayOfWeek - dayOfWeek) + 1
        }

        val daysMissed = (daysPassedThisWeek - daysWithSavings).coerceAtLeast(0)
        
        return Pair(daysWithSavings, daysMissed)
    }

    fun getDateKey(cal: Calendar): String {
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    fun addInstallment(name: String, amount: Double, dueDate: Long, wallet: String, collectedAmount: Double = 0.0, savingType: String = "DAILY") {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(InstallmentEntity(name = name, amount = amount, dueDate = dueDate, wallet = wallet, collectedAmount = collectedAmount, savingType = savingType))
        }
    }

    fun updateInstallment(installment: InstallmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(installment)
        }
    }

    fun markAsSaved(installment: InstallmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val amount = calculatePeriodSaving(installment)
            val updated = installment.copy(
                savedAmount = installment.savedAmount + amount,
                lastSavedDate = System.currentTimeMillis()
            )
            dao.update(updated)
            dao.insertLog(SavingLogEntity(installmentName = installment.name, date = System.currentTimeMillis(), amount = amount))
        }
    }

    fun deleteInstallment(installment: InstallmentEntity) {
        viewModelScope.launch(Dispatchers.IO) { 
            dao.deleteLogsByInstallmentName(installment.name)
            dao.delete(installment) 
        }
    }

    fun calculateDailySaving(installment: InstallmentEntity): Double {
        val daysLeft = calculateDaysLeft(installment.dueDate)
        val remainingAmount = (installment.amount - installment.collectedAmount - installment.savedAmount).coerceAtLeast(0.0)
        return if (daysLeft > 0) remainingAmount / daysLeft else remainingAmount
    }

    fun calculatePeriodSaving(installment: InstallmentEntity): Double {
        val daysLeft = calculateDaysLeft(installment.dueDate)
        val remainingAmount = (installment.amount - installment.collectedAmount - installment.savedAmount).coerceAtLeast(0.0)
        return if (installment.savingType == "WEEKLY") {
            val weeksLeftCeil = kotlin.math.ceil(daysLeft / 7.0).coerceAtLeast(1.0)
            if (daysLeft > 0) kotlin.math.round(remainingAmount / weeksLeftCeil).toDouble() else remainingAmount
        } else {
            if (daysLeft > 0) kotlin.math.round(remainingAmount / daysLeft).toDouble() else remainingAmount
        }
    }

    fun calculateDaysLeft(dueDate: Long): Long {
        val diff = dueDate - System.currentTimeMillis()
        return (TimeUnit.MILLISECONDS.toDays(diff)).coerceAtLeast(0) + 1
    }

    fun isSavedToday(installment: InstallmentEntity): Boolean {
        if (installment.lastSavedDate == 0L) return false
        val lastSaved = Calendar.getInstance().apply { timeInMillis = installment.lastSavedDate }
        val today = Calendar.getInstance()
        return lastSaved.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                lastSaved.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
}
