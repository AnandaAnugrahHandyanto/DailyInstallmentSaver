package com.savares.dailyinstallmentsaver.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import kotlinx.coroutines.launch

class InstallmentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).installmentDao()

    var installments = mutableListOf<InstallmentEntity>()

    fun loadData() {
        viewModelScope.launch {
            installments = dao.getAll().toMutableList()
        }
    }

    fun addInstallment(data: InstallmentEntity) {
        viewModelScope.launch {
            dao.insert(data)
            loadData()
        }
    }
}