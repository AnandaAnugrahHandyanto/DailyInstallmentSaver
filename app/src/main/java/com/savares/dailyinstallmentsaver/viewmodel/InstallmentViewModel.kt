package com.savares.dailyinstallmentsaver.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.model.SavingLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class InstallmentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).installmentDao()

    val installments: StateFlow<List<InstallmentEntity>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingLogs: StateFlow<List<SavingLogEntity>> = dao.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDailySaving: StateFlow<Double> = installments.map { list ->
        list.sumOf { calculateDailySaving(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val walletBreakdown: StateFlow<Map<String, Double>> = installments.map { list ->
        list.groupBy { it.wallet }
            .mapValues { entry -> entry.value.sumOf { calculateDailySaving(it) } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Grouped logs for calendar and stats
    val logsByDate: StateFlow<Map<String, List<SavingLogEntity>>> = savingLogs.map { logs ->
        logs.groupBy { log ->
            val cal = Calendar.getInstance().apply { timeInMillis = log.date }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun addInstallment(name: String, amount: Double, dueDate: Long, wallet: String) {
        viewModelScope.launch {
            val entity = InstallmentEntity(
                name = name,
                amount = amount,
                dueDate = dueDate,
                wallet = wallet
            )
            dao.insert(entity)
        }
    }

    fun deleteInstallment(installment: InstallmentEntity) {
        viewModelScope.launch {
            dao.delete(installment)
        }
    }

    fun markAsSaved(installment: InstallmentEntity) {
        viewModelScope.launch {
            val daily = calculateDailySaving(installment)
            val updated = installment.copy(
                savedAmount = installment.savedAmount + daily,
                lastSavedDate = System.currentTimeMillis()
            )
            dao.update(updated)
            
            dao.insertLog(SavingLogEntity(
                installmentName = installment.name,
                date = System.currentTimeMillis(),
                amount = daily
            ))
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

    fun getSmartSuggestion(installment: InstallmentEntity): Double {
        return calculateDailySaving(installment)
    }
}
