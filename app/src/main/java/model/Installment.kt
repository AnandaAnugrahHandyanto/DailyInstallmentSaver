package com.savares.dailyinstallmentsaver.model

import java.time.LocalDate

data class Installment(
    val name: String,
    val amount: Double,
    val dueDate: LocalDate,
    val wallet: String
) {
    fun daysLeft(today: LocalDate = LocalDate.now()): Long {
        return dueDate.toEpochDay() - today.toEpochDay()
    }

    fun dailySave(today: LocalDate = LocalDate.now()): Double {
        val days = daysLeft(today)
        return if (days > 0) amount / days else amount
    }
}