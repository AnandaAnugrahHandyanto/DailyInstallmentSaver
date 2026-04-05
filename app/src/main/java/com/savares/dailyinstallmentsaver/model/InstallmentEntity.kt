package com.savares.dailyinstallmentsaver.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "installments")
@Serializable
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val savedAmount: Double = 0.0,
    val collectedAmount: Double = 0.0,
    val dueDate: Long,
    val wallet: String,
    val lastSavedDate: Long = 0,
    val savingType: String = "DAILY"
)
