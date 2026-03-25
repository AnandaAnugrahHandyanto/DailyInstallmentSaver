package com.savares.dailyinstallmentsaver.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val wallet: String
)
