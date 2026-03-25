package com.savares.dailyinstallmentsaver.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.savares.dailyinstallmentsaver.R
import com.savares.dailyinstallmentsaver.data.AppDatabase
import com.savares.dailyinstallmentsaver.model.InstallmentEntity
import com.savares.dailyinstallmentsaver.util.CurrencyUtil
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyGoalWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getDatabase(context).installmentDao()
        val installments = dao.getAll().first()
        
        val totalToday = installments.sumOf { calculateDaily(it) }
        val breakdown = installments.groupBy { it.wallet }
            .mapValues { entry -> entry.value.sumOf { calculateDaily(it) } }

        provideContent {
            WidgetContent(context, totalToday, breakdown)
        }
    }

    @Composable
    private fun WidgetContent(context: Context, total: Double, breakdown: Map<String, Double>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.R.color.background_light))
                .padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.widget_title),
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            
            if (total > 0) {
                Text(
                    text = CurrencyUtil.formatCurrency(total),
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.height(4.dp))
                breakdown.forEach { (wallet, amount) ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(text = "$wallet: ", modifier = GlanceModifier.defaultWeight())
                        Text(text = CurrencyUtil.formatCurrency(amount))
                    }
                }
            } else {
                Text(text = context.getString(R.string.widget_empty))
            }
        }
    }

    private fun calculateDaily(installment: InstallmentEntity): Double {
        val diff = installment.dueDate - System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0) + 1
        val remaining = (installment.amount - installment.savedAmount).coerceAtLeast(0.0)
        return remaining / days
    }
}

class DailyGoalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyGoalWidget()
}
