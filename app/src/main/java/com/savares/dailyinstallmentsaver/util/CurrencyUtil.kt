package com.savares.dailyinstallmentsaver.util

import java.text.NumberFormat
import java.util.*

object CurrencyUtil {
    private val localeID = Locale("id", "ID")
    
    // ThreadLocal is faster than synchronization for NumberFormat
    private val numberFormatThreadLocal = object : ThreadLocal<NumberFormat>() {
        override fun initialValue(): NumberFormat {
            return NumberFormat.getCurrencyInstance(localeID).apply {
                maximumFractionDigits = 0
            }
        }
    }

    fun formatCurrency(amount: Double): String {
        return numberFormatThreadLocal.get()?.format(amount)?.replace("Rp", "Rp ") ?: ""
    }
}
