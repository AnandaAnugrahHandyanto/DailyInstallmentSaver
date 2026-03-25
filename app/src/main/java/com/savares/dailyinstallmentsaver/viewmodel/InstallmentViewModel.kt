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

data class DashboardUiState(
    val installments: List<InstallmentEntity> = emptyList(),
    val totalDailySaving: Double = 0.0,
    val walletBreakdown: Map<String, Double> = emptyMap()
)

data class StatsUiState(
    val logsByDate: Map<String, List<SavingLogEntity>> = emptyMap(),
    val trendPoints: List<Float> = emptyList()
)

class InstallmentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).installmentDao()

    val installments: StateFlow<List<InstallmentEntity>> = dao.getAll()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingLogs: StateFlow<List<SavingLogEntity>> = dao.getAllLogs()
        .map { it.reversed() }
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
        installments, totalDailySaving, walletBreakdown
    ) { inst, total, wallet ->
        DashboardUiState(inst, total, wallet)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    val logsByDate: StateFlow<Map<String, List<SavingLogEntity>>> = savingLogs.map { logs ->
        logs.groupBy { log ->
            val cal = Calendar.getInstance().apply { timeInMillis = log.date }
            getDateKey(cal)
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val statsUiState: StateFlow<StatsUiState> = logsByDate.map { logs ->
        StatsUiState(
            logsByDate = logs,
            trendPoints = calculateTrendPoints(logs)
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

    fun getDateKey(cal: Calendar): String {
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    fun getDao(): InstallmentDao = dao

    fun addInstallment(name: String, amount: Double, dueDate: Long, wallet: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(InstallmentEntity(name = name, amount = amount, dueDate = dueDate, wallet = wallet))
        }
    }

    fun updateInstallment(installment: InstallmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(installment)
        }
    }

    fun markAsSaved(installment: InstallmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val daily = calculateDailySaving(installment)
            val updated = installment.copy(
                savedAmount = installment.savedAmount + daily,
                lastSavedDate = System.currentTimeMillis()
            )
            dao.update(updated)
            dao.insertLog(SavingLogEntity(installmentName = installment.name, date = System.currentTimeMillis(), amount = daily))
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
        val remainingAmount = (installment.amount - installment.savedAmount).coerceAtLeast(0.0)
        return if (daysLeft > 0) remainingAmount / daysLeft else remainingAmount
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
