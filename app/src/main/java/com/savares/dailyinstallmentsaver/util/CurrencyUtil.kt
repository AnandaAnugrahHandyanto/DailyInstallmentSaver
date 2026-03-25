package com.savares.dailyinstallmentsaver.util

import java.text.NumberFormat
import java.util.*

object CurrencyUtil {
    fun formatCurrency(amount: Double): String {
        val localeID = Locale("id", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount).replace("Rp", "Rp ")
    }
}
