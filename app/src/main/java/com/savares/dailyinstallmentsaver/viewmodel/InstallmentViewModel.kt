package com.savares.dailyinstallmentsaver.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class InstallmentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).installmentDao()

    val installments: StateFlow<List<InstallmentEntity>> = dao.getAll()
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

    private fun calculateDailySaving(installment: InstallmentEntity): Double {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = installment.dueDate - currentTime
        val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillis).coerceAtLeast(0) + 1
        return installment.amount / daysLeft
    }
}
