package com.savares.dailyinstallmentsaver.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "saving_logs")
@Serializable
data class SavingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val installmentName: String,
    val date: Long,
    val amount: Double
)
