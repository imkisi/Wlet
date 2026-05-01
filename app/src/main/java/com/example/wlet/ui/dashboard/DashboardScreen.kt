package com.example.wlet.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wlet.data.local.entities.Category
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.home.formatCurrency
import com.example.wlet.ui.theme.WletTheme
import java.util.*
import kotlin.math.*

/**
 * Main content for the Dashboard Screen.
 * Provides summaries of expenses across different timeframes with a bubble cluster visualizer.
 */
@Composable
fun DashboardScreenContent(viewModel: FinanceViewModel) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    DashboardScreenUI(transactions, categories)
}

@Composable
fun DashboardScreenUI(
    transactions: List<Transaction>,
    categories: List<Category>
) {
    var selectedTab by remember { mutableStateOf(1) } // 0: Minggu, 1: Bulan, 2: Tahun
    val tabs = listOf("Minggu", "Bulan", "Tahun")

    val filteredTransactions = remember(transactions, selectedTab) {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        transactions.filter { 
            it.transactionType == "EXPENSE" && when (selectedTab) {
                0 -> { // Weekly
                    val startOfWeek = calendar.apply { 
                        timeInMillis = now
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                    it.date >= startOfWeek
                }
                1 -> { // Monthly
                    val startOfMonth = calendar.apply { 
                        timeInMillis = now
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                    it.date >= startOfMonth
                }
                2 -> { // Yearly
                    val startOfYear = calendar.apply { 
                        timeInMillis = now
                        set(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                    it.date >= startOfYear
                }
                else -> true
            }
        }
    }

    val totalExpense = filteredTransactions.sumOf { it.amount }
    
    // Custom Palette based on requirements
    val categoryColors = listOf(
        Color(0xFFF4EFEE), Color(0xFFFFD3CF), Color(0xFFFF88AA),
        Color(0xFFFFB1C1), Color(0xFFD47AFF), Color(0xFF924FF1),
        Color(0xFF7338D2), Color(0xFF5511BB), Color(0xFF4527A0),
        Color(0xFF2D135F)
    )

    val categoryExpenses = remember(filteredTransactions, categories) {
        filteredTransactions.groupBy { it.categoryId }
            .entries
            .toList()
            .mapIndexed { index, entry ->
                val catId = entry.key
                val trans = entry.value
                val category = categories.find { it.id == catId }
                val amount = trans.sumOf { it.amount }
                val color = categoryColors[index % categoryColors.size]
                Triple(category, amount, color)
            }.sortedByDescending { it.second }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0ECE9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            // Tab Selector - Pill shaped at top center
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .wrapContentWidth()
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Row(
                    modifier = Modifier.padding(0.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            onClick = { selectedTab = index },
                            color = if (isSelected) Color.Blue else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                color = if (isSelected) Color.White else Color.Black,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bubble Visualization cluster - Central element
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BubbleCluster(categoryExpenses, totalExpense)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Expense Display - Below bubbles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 150.dp) // Leave space for Dock
            ) {
                Text(text = "Terpakai", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatCurrency(totalExpense),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Bubble position data class
 */
private data class BubbleNode(
    val category: Category?,
    val amount: Double,
    val color: Color,
    val size: Float,
    var x: Float,
    var y: Float
)

/**
 * Renders bubbles packed together containing category info.
 * Bubbles stick together but do not overlap using a custom packing logic.
 */
@Composable
fun BubbleCluster(
    categoryExpenses: List<Triple<Category?, Double, Color>>,
    total: Double
) {
    if (total == 0.0) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Belum ada data", color = Color.Gray)
        }
    } else {
        val bubbleNodes = remember(categoryExpenses, total) {
            val nodes = categoryExpenses.map { (cat, amt, color) ->
                val ratio = (amt / total).toFloat()
                // Size represents total expense: base 100 + scale
                val size = 110f + (ratio * 140f)
                BubbleNode(cat, amt, color, size, 0f, 0f)
            }
            packBubbles(nodes)
            nodes
        }

        // Floating animation effect
        val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            bubbleNodes.forEachIndexed { index, node ->
                val floatX = sin(phase + index) * 6f
                val floatY = cos(phase + index * 0.7f) * 6f
                
                val ratio = (node.amount / total).toFloat()

                Surface(
                    modifier = Modifier
                        .offset(x = (node.x + floatX).dp, y = (node.y + floatY).dp)
                        .size(node.size.dp),
                    shape = CircleShape,
                    color = node.color,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = node.category?.name ?: "Lainnya",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (10 + (ratio * 5)).sp,
                                color = if (isColorDark(node.color)) Color.White else Color.Black
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(node.amount),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (9 + (ratio * 6)).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isColorDark(node.color)) Color.White else Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Packs bubbles tightly around the center (0,0) without overlapping.
 */
private fun packBubbles(nodes: List<BubbleNode>) {
    if (nodes.isEmpty()) return
    
    val sorted = nodes.sortedByDescending { it.size }
    sorted[0].x = 0f
    sorted[0].y = 0f
    
    for (i in 1 until sorted.size) {
        val node = sorted[i]
        val radius = node.size / 2f
        
        var angle = 0f
        var distance = 0f
        var placed = false
        
        while (!placed && distance < 1200f) {
            val tx = cos(angle) * distance
            val ty = sin(angle) * distance
            
            var collision = false
            for (j in 0 until i) {
                val other = sorted[j]
                val dx = tx - other.x
                val dy = ty - other.y
                val distSq = dx * dx + dy * dy
                val minDist = radius + (other.size / 2f)
                if (distSq < minDist * minDist) {
                    collision = true
                    break
                }
            }
            
            if (!collision) {
                node.x = tx
                node.y = ty
                placed = true
            } else {
                angle += 0.2f
                distance += 0.8f
            }
        }
    }
}

/**
 * Helper to determine if text should be white or black based on background luminance.
 */
fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    WletTheme {
        val sampleCategories = listOf(
            Category(id = 1, name = "Belanja", type = "EXPENSE"),
            Category(id = 2, name = "Makanan", type = "EXPENSE"),
            Category(id = 3, name = "Transport", type = "EXPENSE"),
            Category(id = 4, name = "Lainnya", type = "EXPENSE")
        )
        val sampleTransactions = listOf(
            Transaction(name = "T1", amount = 1500000.0, date = System.currentTimeMillis(), description = "", categoryId = 1, transactionType = "EXPENSE"),
            Transaction(name = "T2", amount = 800000.0, date = System.currentTimeMillis(), description = "", categoryId = 2, transactionType = "EXPENSE"),
            Transaction(name = "T3", amount = 400000.0, date = System.currentTimeMillis(), description = "", categoryId = 3, transactionType = "EXPENSE"),
            Transaction(name = "T4", amount = 100000.0, date = System.currentTimeMillis(), description = "", categoryId = 4, transactionType = "EXPENSE")
        )
        DashboardScreenUI(transactions = sampleTransactions, categories = sampleCategories)
    }
}
