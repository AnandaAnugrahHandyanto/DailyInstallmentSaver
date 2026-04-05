package com.savares.dailyinstallmentsaver.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.SavingLogEntityimport kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.round

class MarkAsSavedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val installmentId = intent.getIntExtra(ReminderWorker.KEY_INSTALLMENT_ID, -1)
        if (installmentId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).installmentDao()
                val installment = dao.getAllList().find { it.id == installmentId }
                    ?: return@launch

                val daysLeft = calculateDaysLeft(installment.dueDate)
                val remaining = (installment.amount - installment.collectedAmount - installment.savedAmount)
                    .coerceAtLeast(0.0)
                val amount = if (installment.savingType == "WEEKLY") {
                    val weeksLeft = ceil(daysLeft / 7.0).coerceAtLeast(1.0)
                    if (daysLeft > 0) round(remaining / weeksLeft).toDouble() else remaining
                } else {
                    if (daysLeft > 0) round(remaining / daysLeft).toDouble() else remaining
                }

                dao.update(
                    installment.copy(
                        savedAmount = installment.savedAmount + amount,
                        lastSavedDate = System.currentTimeMillis()
                    )
                )
                dao.insertLog(
                    SavingLogEntity(
                        installmentName = installment.name,
                        date = System.currentTimeMillis(),
                        amount = amount
                    )
                )

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(installmentId)

                ReminderScheduler.cancelReminder(context, installmentId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.notification_saved_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun calculateDaysLeft(dueDate: Long): Long {
        val diff = dueDate - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0) + 1
    }
}
