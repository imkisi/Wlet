package com.example.wlet.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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

suspend fun updateWletWidget(context: Context) {
    WletWidget().updateAll(context)
}

class WletWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as WletApplication).repository
        val settingsManager = (context.applicationContext as WletApplication).settingsManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        val transactions = repository.allTransactions.first()
        val todaySpent = transactions
            .filter { it.transactionType == "EXPENSE" && it.date >= startOfToday }
            .sumOf { it.amount }

        val currencyCode = settingsManager.currency

        provideContent {
            WidgetContent(todaySpent, currencyCode)
        }
    }

    @Composable
    private fun WidgetContent(todaySpent: Double, currencyCode: String) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFF0ECE9))
                .padding(12.dp)
        ) {
            // Top Content
            Column(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hari ini",
                    style = TextStyle(
                        color = androidx.glance.color.ColorProvider(day = Color.Gray, night = Color.Gray),
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = formatCurrency(todaySpent, currencyCode),
                    style = TextStyle(
                        color = androidx.glance.color.ColorProvider(day = Color.Gray, night = Color.Gray),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Bottom Right Content
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
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.add),
                        contentDescription = "Add",
                        modifier = GlanceModifier.size(24.dp)
                    )
                }
            }
        }
    }
}