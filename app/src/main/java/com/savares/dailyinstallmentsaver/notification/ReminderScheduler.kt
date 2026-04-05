package com.savares.dailyinstallmentsaver.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(context: Context, installment: InstallmentEntity) {
        val inputData = Data.Builder()
            .putInt(ReminderWorker.KEY_INSTALLMENT_ID, installment.id)
            .build()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName(installment.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelReminder(context: Context, installmentId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(installmentId))
    }

    fun scheduleAll(context: Context, installments: List<InstallmentEntity>) {
        installments.forEach { scheduleReminder(context, it) }
    }

    private fun workName(installmentId: Int) = "reminder_$installmentId"
}
