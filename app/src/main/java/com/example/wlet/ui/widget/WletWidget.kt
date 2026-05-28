package com.example.wlet.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.wlet.MainActivity
import com.example.wlet.R
import com.example.wlet.WletApplication
import com.example.wlet.ui.util.formatCurrency
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Helper function to trigger a widget update from anywhere in the app.
 */
suspend fun updateWletWidget(context: Context) {
    WletWidget().updateAll(context)
}

class WletWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as WletApplication).repository
        val settingsManager = (context.applicationContext as WletApplication).settingsManager

        // Logic: Calculate total expenses for today
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        val currencyCode = settingsManager.currency

        repository.allTransactions.collect { transactions ->
            val todaySpent = transactions
                .filter { it.transactionType == "EXPENSE" && it.date >= startOfToday }
                .sumOf { it.amount }

            provideContent {
                WidgetContent(todaySpent, currencyCode)
            }
        }
    }

    @Composable
    private fun WidgetContent(todaySpent: Double, currencyCode: String) {
        val context = LocalContext.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFF0ECE9))
                .padding(12.dp)
        ) {
            // Top Left: Today's Spent Display
            Column(
                modifier = GlanceModifier.fillMaxWidth().run {
                    this
                }
            ) {
                Text(
                    text = context.getString(R.string.today_spent),
                    style = TextStyle(
                        color = ColorProvider(Color.Gray),
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = formatCurrency(todaySpent, currencyCode),
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Bottom Right: Add Transaction Button
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .background(ImageProvider(R.drawable.add_bg_circle))
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.add),
                        contentDescription = "Add",
                        modifier = GlanceModifier.size(24.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(Color.White))
                    )
                }
            }
        }
    }
}
