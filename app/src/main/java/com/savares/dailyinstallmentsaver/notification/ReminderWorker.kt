package com.savares.dailyinstallmentsaver.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.savares.dailyinstallmentsaver.MainActivity
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_INSTALLMENT_ID = "installmentId"
        const val CHANNEL_ID = "reminder_channel"
        private const val HOUR_START = 8
        private const val HOUR_END = 22
    }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return Result.success()

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < HOUR_START || hour >= HOUR_END) return Result.success()

        val installmentId = inputData.getInt(KEY_INSTALLMENT_ID, -1)
        if (installmentId == -1) return Result.failure()

        val dao = AppDatabase.getDatabase(applicationContext).installmentDao()
        val installment = dao.getAll().first().find { it.id == installmentId }
            ?: return Result.success()

        val shouldNotify = when (installment.savingType) {
            "WEEKLY" -> shouldNotifyWeekly(installment)
            else -> !isSavedToday(installment)
        }

        if (shouldNotify) {
            val message = if (installment.savingType == "WEEKLY") {
                applicationContext.getString(R.string.notification_weekly_message)
            } else {
                applicationContext.getString(R.string.notification_daily_message)
            }
            showNotification(installment.id, message)
        }

        return Result.success()
    }

    private fun isSavedToday(installment: InstallmentEntity): Boolean {
        if (installment.lastSavedDate == 0L) return false
        val lastSaved = Calendar.getInstance().apply { timeInMillis = installment.lastSavedDate }
        val today = Calendar.getInstance()
        return lastSaved.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                lastSaved.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun shouldNotifyWeekly(installment: InstallmentEntity): Boolean {
        val today = Calendar.getInstance()
        val dueCal = Calendar.getInstance().apply { timeInMillis = installment.dueDate }
        if (today.get(Calendar.DAY_OF_WEEK) != dueCal.get(Calendar.DAY_OF_WEEK)) return false
        if (installment.lastSavedDate == 0L) return true
        val lastSaved = Calendar.getInstance().apply { timeInMillis = installment.lastSavedDate }
        return !(lastSaved.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                lastSaved.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR))
    }

    private fun showNotification(installmentId: Int, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val clickIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_INSTALLMENT_ID, installmentId)
        }
        val clickPendingIntent = PendingIntent.getActivity(
            applicationContext,
            installmentId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIntent = Intent(applicationContext, MarkAsSavedReceiver::class.java).apply {
            putExtra(KEY_INSTALLMENT_ID, installmentId)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            installmentId,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.notification_reminder_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(clickPendingIntent)
            .addAction(
                0,
                applicationContext.getString(R.string.notification_action_mark_saved),
                actionPendingIntent
            )
            .build()

        notificationManager.notify(installmentId, notification)
    }
}
