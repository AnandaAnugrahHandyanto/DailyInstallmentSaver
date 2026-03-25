package com.savares.dailyinstallmentsaver.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).installmentDao()
        val installments = dao.getAll().first()

        if (installments.isNotEmpty()) {
            val totalSaving = installments.sumOf { calculateDaily(it) }
            val details = installments.joinToString(", ") { "${it.name}: ${formatCurrency(calculateDaily(it))}" }
            
            showNotification(
                applicationContext.getString(R.string.notification_title),
                applicationContext.getString(R.string.notification_content, formatCurrency(totalSaving), details)
            )
        }

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    private fun calculateDaily(installment: InstallmentEntity): Double {
        val diff = installment.dueDate - System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0) + 1
        return installment.amount / days
    }
}
