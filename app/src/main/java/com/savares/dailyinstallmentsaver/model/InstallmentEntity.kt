package com.savares.dailyinstallmentsaver.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val wallet: String,
    val savedAmount: Double = 0.0,
    val lastSavedDate: Long = 0L, // Timestamp of last "Mark as Saved"
    val createdAt: Long = System.currentTimeMillis()
)
