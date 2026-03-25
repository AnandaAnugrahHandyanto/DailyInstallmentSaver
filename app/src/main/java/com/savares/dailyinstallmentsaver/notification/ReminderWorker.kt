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
import com.savares.dailyinstallmentsaver.util.CurrencyUtil
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return Result.success()

        val dao = AppDatabase.getDatabase(applicationContext).installmentDao()
        val installments = dao.getAll().first()

        if (installments.isNotEmpty()) {
            var totalToday = 0.0
            var missedSomething = false
            
            val today = Calendar.getInstance()
            
            installments.forEach {
                val daily = calculateDaily(it)
                totalToday += daily
                
                if (it.lastSavedDate != 0L) {
                    val last = Calendar.getInstance().apply { timeInMillis = it.lastSavedDate }
                    if (today.get(Calendar.DAY_OF_YEAR) - last.get(Calendar.DAY_OF_YEAR) > 1 || 
                        today.get(Calendar.YEAR) != last.get(Calendar.YEAR)) {
                        missedSomething = true
                    }
                } else {
                    missedSomething = true
                }
            }

            val title = if (missedSomething) {
                applicationContext.getString(R.string.notification_missed_title)
            } else {
                applicationContext.getString(R.string.notification_title)
            }

            val content = applicationContext.getString(
                R.string.notification_content, 
                CurrencyUtil.formatCurrency(totalToday)
            )
            
            showNotification(title, content)
        }

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun calculateDaily(installment: InstallmentEntity): Double {
        val diff = installment.dueDate - System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0) + 1
        val remaining = (installment.amount - installment.savedAmount).coerceAtLeast(0.0)
        return remaining / days
    }
}
