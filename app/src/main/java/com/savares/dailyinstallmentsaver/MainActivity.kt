package com.savares.dailyinstallmentsaver

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.savares.dailyinstallmentsaver.ui.DashboardScreen
import com.savares.dailyinstallmentsaver.ui.theme.DailyInstallmentSaverTheme
import androidx.work.*
import com.savares.dailyinstallmentsaver.notification.ReminderWorker
import java.util.concurrent.TimeUnit

fun scheduleReminder(context: Context) {
    val work = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_reminder",
        ExistingPeriodicWorkPolicy.KEEP,
        work
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyInstallmentSaverTheme {
                DashboardScreen()
            }
        }
    }
}
