package com.example.wlet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wlet.R
import com.example.wlet.data.local.entities.Category
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.theme.RobotoMono
import com.example.wlet.ui.theme.WletTheme
import com.example.wlet.ui.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

/**
 * Provides the standard vertical gradient used across the application for cards and containers.
 */
@Composable
fun getVerticalThemedGradient(): Brush {
    return Brush.verticalGradient(
        0.0f to Color.White,
        0.5f to Color.White,
        1.0f to Color(0xFFF0ECE9)
    )
}

/**
 * HomeScreenContent manages the main view of the application.
 * Displays total balance and transaction list filtered for the current month.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    viewModel: FinanceViewModel,
    onShowEditSheet: (Transaction) -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    val currentCurrency by viewModel.currency.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var isDetailSheetVisible by remember { mutableStateOf(false) }

    // Optimization: Auto-hide transactions from more than a month ago
    val filteredTransactions = remember(allTransactions) {
        val oneMonthAgo = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.timeInMillis
        allTransactions.filter { it.date >= oneMonthAgo }
    }

    val appLocale = remember(currentLanguage) { Locale(currentLanguage) }

    val currentDate = remember(currentLanguage) {
        SimpleDateFormat("EEEE, d MMMM", appLocale).format(Date())
    }

    // Optimization: Derived state for grouping to prevent expensive re-calculations
    val groupedTransactions = remember(filteredTransactions, currentLanguage) {
        filteredTransactions.groupBy {
            SimpleDateFormat("dd MMM", appLocale).format(Date(it.date))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0ECE9))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                val totalBalance = allTransactions.sumOf { 
                    if (it.transactionType == "INCOME") it.amount else -it.amount 
                }
                HeaderSection(
                    tanggal = currentDate,
                    totalSaldo = formatCurrency(totalBalance, currentCurrency)
                )
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_transactions_month),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontFamily = RobotoMono
                        )
                    }
                }
            } else {
                items(groupedTransactions.entries.toList()) { entry ->
                    DailyTransactionCard(
                        date = entry.key,
                        dailyTransactions = entry.value,
                        categories = categories,
                        currencyCode = currentCurrency,
                        onItemClick = { transaction ->
                            selectedTransaction = transaction
                            isDetailSheetVisible = true
                        }
                    )
                }
            }
        }

        if (isDetailSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isDetailSheetVisible = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                EditDeleteSheetContent(
                    transaction = selectedTransaction,
                    categories = categories,
                    currencyCode = currentCurrency,
                    onClose = { isDetailSheetVisible = false },
                    onEdit = { 
                        isDetailSheetVisible = false
                        selectedTransaction?.let { onShowEditSheet(it) }
                    },
                    onDelete = {
                        selectedTransaction?.let { viewModel.delete(it) }
                        isDetailSheetVisible = false
                    }
                )
            }
        }
    }
}

/**
 * Reusable Bottom Sheet content for adding or editing transactions.
 */
@Composable
fun AddEditTransactionSheetContent(
    title: String,
    transaction: Transaction? = null,
    categories: List<Category>,
    onSave: (String, Double, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(transaction?.name ?: "") }
    var amount by remember { mutableStateOf(transaction?.amount?.toLong()?.toString() ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var transactionType by remember { mutableStateOf(transaction?.transactionType ?: "EXPENSE") }
    
    val initialCategoryName = categories.find { it.id == transaction?.categoryId }?.name ?: ""
    var categoryInput by remember { mutableStateOf(initialCategoryName) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = RobotoMono
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction Type Selector (Income/Expense)
        Surface(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            color = Color(0xFFF2F0EB)
        ) {
            Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TransactionTypeTab(
                    selected = transactionType == "EXPENSE",
                    text = stringResource(R.string.expense),
                    modifier = Modifier.weight(1f),
                    onClick = { transactionType = "EXPENSE" }
                )
                TransactionTypeTab(
                    selected = transactionType == "INCOME",
                    text = stringResource(R.string.income),
                    modifier = Modifier.weight(1f),
                    onClick = { transactionType = "INCOME" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name), fontFamily = RobotoMono) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoMono)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
            label = { Text(stringResource(R.string.amount), fontFamily = RobotoMono) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoMono)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = categoryInput,
                onValueChange = { 
                    categoryInput = it
                    expanded = true
                },
                label = { Text(stringResource(R.string.category), fontFamily = RobotoMono) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoMono),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(painter = painterResource(id = R.drawable.categories), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
            )
            
            DropdownMenu(
                expanded = expanded && categories.any { it.type == transactionType },
                onDismissRequest = { expanded = false },
                properties = androidx.compose.ui.window.PopupProperties(focusable = false)
            ) {
                categories.filter { it.type == transactionType }.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name, fontFamily = RobotoMono) },
                        onClick = {
                            categoryInput = category.name
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.description_optional), fontFamily = RobotoMono) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoMono)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && amt > 0 && categoryInput.isNotBlank()) {
                    onSave(name, amt, description, categoryInput, transactionType)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = RobotoMono)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TransactionTypeTab(selected: Boolean, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) Color.Blue else Color.Transparent,
        contentColor = if (selected) Color.White else Color.Gray
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontFamily = RobotoMono)
        }
    }
}

@Composable
fun HeaderSection(tanggal: String, totalSaldo: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 100.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = Color.Blue) {
            Text(
                text = tanggal,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontFamily = RobotoMono
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.current_balance), color = Color.Gray, style = MaterialTheme.typography.bodyMedium, fontFamily = RobotoMono)
        Text(
            text = totalSaldo,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontFamily = RobotoMono),
            color = Color.Black,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun DailyTransactionCard(date: String, dailyTransactions: List<Transaction>, categories: List<Category>, currencyCode: String, onItemClick: (Transaction) -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.background(brush = getVerticalThemedGradient()).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = date, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
                Row {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = stringResource(R.string.in_label), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontFamily = RobotoMono)
                        Text(formatCurrency(dailyTransactions.filter { it.transactionType == "INCOME" }.sumOf { it.amount }, currencyCode), style = MaterialTheme.typography.bodySmall, fontFamily = RobotoMono)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = stringResource(R.string.out_label), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontFamily = RobotoMono)
                        Text(formatCurrency(dailyTransactions.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }, currencyCode), style = MaterialTheme.typography.bodySmall, fontFamily = RobotoMono)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            dailyTransactions.forEach { TransactionRow(it, categories.find { c -> c.id == it.categoryId }?.name ?: "Umum", currencyCode, onClick = { onItemClick(it) }) }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, categoryName: String, currencyCode: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.name, style = MaterialTheme.typography.bodyLarge, fontFamily = RobotoMono)
            Text(categoryName, color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontFamily = RobotoMono)
        }
        val colorScheme = if (transaction.transactionType == "INCOME") Color.Blue else Color.Red
        Surface(color = colorScheme.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
            Text(
                text = (if (transaction.transactionType == "INCOME") "+" else "-") + formatCurrency(transaction.amount, currencyCode),
                color = colorScheme,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
            )
        }
    }
}

@Composable
fun FloatingDock(onHomeClick: () -> Unit, onDashboardClick: () -> Unit, onSettingsClick: () -> Unit, onAddClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Surface(shape = CircleShape, shadowElevation = 6.dp, color = Color.White) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onHomeClick, modifier = Modifier.size(48.dp)) { Icon(painterResource(R.drawable.home), "Home", Modifier.size(24.dp), Color.Black) }
                IconButton(onClick = onDashboardClick, modifier = Modifier.size(48.dp)) { Icon(painterResource(R.drawable.dashboard), "Dashboard", Modifier.size(24.dp), Color.Black) }
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(48.dp)) { Icon(painterResource(R.drawable.settings), "Settings", Modifier.size(24.dp), Color.Black) }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        FloatingActionButton(onClick = onAddClick, shape = CircleShape, containerColor = Color.Blue, contentColor = Color.White, elevation = FloatingActionButtonDefaults.elevation(6.dp), modifier = Modifier.size(64.dp)) {
            Icon(painterResource(R.drawable.add), "Add", Modifier.size(32.dp))
        }
    }
}

@Composable
fun EditDeleteSheetContent(transaction: Transaction?, categories: List<Category>, currencyCode: String, onClose: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val categoryName = categories.find { it.id == transaction?.categoryId }?.name ?: "Tanpa Kategori"
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()) {
        Text(text = stringResource(R.string.transaction_details), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(24.dp))
        DetailRow(stringResource(R.string.name), transaction?.name ?: "")
        DetailRow(stringResource(R.string.amount), formatCurrency(transaction?.amount ?: 0.0, currencyCode), isAmount = true, type = transaction?.transactionType)
        DetailRow(stringResource(R.string.category), categoryName)
        if (!transaction?.description.isNullOrBlank()) DetailRow(stringResource(R.string.description), transaction?.description ?: "")
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onEdit, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Blue), shape = RoundedCornerShape(16.dp)) {
                Icon(painterResource(R.drawable.edit), "Edit", Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.edit), fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
            }
            Button(onClick = onDelete, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RoundedCornerShape(16.dp)) {
                Icon(painterResource(R.drawable.delete), "Delete", Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.delete), fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
            Text(stringResource(R.string.close), fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String, isAmount: Boolean = false, type: String? = null) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontFamily = RobotoMono)
        val color = if (isAmount) (if (type == "INCOME") Color.Blue else Color.Red) else Color.Black
        Text(text = if (isAmount) (if (type == "INCOME") "+" else "-") + value else value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontFamily = RobotoMono), color = color)
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderSectionPreview() { WletTheme { HeaderSection("Senin, 10 Maret", "Rp 500.000") } }
