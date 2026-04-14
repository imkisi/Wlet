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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wlet.R
import com.example.wlet.data.local.entities.Category
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.theme.WletTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(viewModel: FinanceViewModel) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    val sheetState = rememberModalBottomSheetState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var isSheetVisible by remember { mutableStateOf(false) }
    var isEditDialogOpen by remember { mutableStateOf(false) }

    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
    }

    val groupedTransactions = transactions.groupBy {
        SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(it.date))
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0ECE9))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                val totalBalance = transactions.sumOf { 
                    if (it.transactionType == "INCOME") it.amount else -it.amount 
                }
                HeaderSection(
                    tanggal = currentDate,
                    totalSaldo = formatCurrency(totalBalance),
                    onDateClick = { /* navController.navigate("monthly_report") */ }
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada transaksi", color = Color.Gray)
                    }
                }
            } else {
                groupedTransactions.forEach { (date, dailyList) ->
                    item {
                        DailyTransactionCard(
                            date = date,
                            dailyTransactions = dailyList,
                            categories = categories,
                            onItemClick = { transaction ->
                                selectedTransaction = transaction
                                isSheetVisible = true
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        if (isSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isSheetVisible = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                EditDeleteSheetContent(
                    transaction = selectedTransaction,
                    categories = categories,
                    onClose = { isSheetVisible = false },
                    onEdit = { 
                        isSheetVisible = false
                        isEditDialogOpen = true 
                    },
                    onDelete = {
                        selectedTransaction?.let { viewModel.delete(it) }
                        isSheetVisible = false
                    }
                )
            }
        }

        if (isEditDialogOpen && selectedTransaction != null) {
            AddEditTransactionDialog(
                title = "Edit Transaksi",
                transaction = selectedTransaction,
                categories = categories,
                onDismiss = { isEditDialogOpen = false },
                onSave = { name, amount, desc, catId, type ->
                    viewModel.update(
                        selectedTransaction!!.copy(
                            name = name,
                            amount = amount,
                            description = desc,
                            categoryId = catId,
                            transactionType = type
                        )
                    )
                    isEditDialogOpen = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    title: String,
    transaction: Transaction? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Long?, String) -> Unit
) {
    var name by remember { mutableStateOf(transaction?.name ?: "") }
    var amount by remember { mutableStateOf(transaction?.amount?.toLong()?.toString() ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var transactionType by remember { mutableStateOf(transaction?.transactionType ?: "EXPENSE") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(transaction?.categoryId) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = transactionType == "EXPENSE",
                        onClick = { 
                            transactionType = "EXPENSE"
                            selectedCategoryId = null
                        },
                        label = { Text("Pengeluaran") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == "INCOME",
                        onClick = { 
                            transactionType = "INCOME"
                            selectedCategoryId = null
                        },
                        label = { Text("Pemasukan") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("Nominal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Pilih Kategori",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(painter = painterResource(id = R.drawable.settings), contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        val filteredCategories = categories.filter { it.type == transactionType }
                        if (filteredCategories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Tidak ada kategori") },
                                onClick = { expanded = false }
                            )
                        } else {
                            filteredCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && amt > 0) {
                            onSave(name, amt, description, selectedCategoryId, transactionType)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HeaderSection(tanggal: String, totalSaldo: String, onDateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Blue,
            modifier = Modifier.clickable { onDateClick() }
        ) {
            Text(
                text = tanggal,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Saldo Saat Ini", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = totalSaldo,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            ),
            color = Color.Black,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun DailyTransactionCard(
    date: String,
    dailyTransactions: List<Transaction>,
    categories: List<Category>,
    onItemClick: (Transaction) -> Unit
) {
    val totalIncome = dailyTransactions.filter { it.transactionType == "INCOME" }.sumOf { it.amount }
    val totalExpense = dailyTransactions.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }

    val gradientBrush = Brush.verticalGradient(
        0.0f to Color.White,
        0.5f to Color.White,
        1.0f to Color(0xFFF0ECE9)
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(brush = gradientBrush)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = date, fontWeight = FontWeight.Bold)
                Row {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Masuk", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Text(formatCurrency(totalIncome), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Keluar", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Text(formatCurrency(totalExpense), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            dailyTransactions.forEach { transaction ->
                val categoryName = categories.find { it.id == transaction.categoryId }?.name ?: "Tanpa Kategori"
                TransactionRow(transaction, categoryName, onClick = { onItemClick(transaction) })
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, categoryName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.name, style = MaterialTheme.typography.bodyLarge)
            Text(categoryName, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }

        val colorScheme = if (transaction.transactionType == "INCOME") Color.Blue else Color.Red
        val bgColor = colorScheme.copy(alpha = 0.1f)

        Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = (if (transaction.transactionType == "INCOME") "+" else "-") + formatCurrency(transaction.amount),
                color = colorScheme,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun FloatingDock(onSettingsClick: () -> Unit, onHomeClick: () -> Unit, onAddClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Surface(
            shape = CircleShape,
            shadowElevation = 6.dp,
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                IconButton(onClick = onHomeClick, modifier = Modifier.size(48.dp)) {
                    Icon(painter = painterResource(id = R.drawable.home), contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.Black)
                }
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(48.dp)) {
                    Icon(painter = painterResource(id = R.drawable.settings), contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        FloatingActionButton(
            onClick = onAddClick,
            shape = CircleShape,
            containerColor = Color.Blue,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier.size(64.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.add), contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EditDeleteSheetContent(
    transaction: Transaction?, 
    categories: List<Category>,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryName = categories.find { it.id == transaction?.categoryId }?.name ?: "Tanpa Kategori"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Rincian Transaksi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        DetailRow(label = "Nama", value = transaction?.name ?: "")
        DetailRow(label = "Nominal", value = formatCurrency(transaction?.amount ?: 0.0), isAmount = true, type = transaction?.transactionType)
        DetailRow(label = "Kategori", value = categoryName)
        if (!transaction?.description.isNullOrBlank()) {
            DetailRow(label = "Deskripsi", value = transaction?.description ?: "")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.edit), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.delete), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hapus", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tutup", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String, isAmount: Boolean = false, type: String? = null) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        val color = if (isAmount) {
            if (type == "INCOME") Color.Blue else Color.Red
        } else {
            Color.Black
        }
        Text(
            text = if (isAmount) (if (type == "INCOME") "+" else "-") + value else value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}

@Preview(showBackground = true)
@Composable
fun HeaderSectionPreview() {
    WletTheme {
        HeaderSection(
            tanggal = "Senin, 10 Maret",
            totalSaldo = "Rp 500.000",
            onDateClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DailyTransactionCardPreview() {
    WletTheme {
        DailyTransactionCard(
            date = "10 Mar",
            dailyTransactions = listOf(
                Transaction(name = "Makan Siang", amount = 50000.0, date = 0L, description = null, categoryId = null, transactionType = "EXPENSE"),
                Transaction(name = "Gaji", amount = 1000000.0, date = 0L, description = null, categoryId = null, transactionType = "INCOME")
            ),
            categories = emptyList(),
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FloatingDockPreview() {
    WletTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FloatingDock(onSettingsClick = {}, onHomeClick = {}, onAddClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditTransactionDialogPreview() {
    WletTheme {
        AddEditTransactionDialog(
            title = "Tambah Transaksi",
            categories = listOf(
                Category(id = 1, name = "Makanan", type = "EXPENSE"),
                Category(id = 2, name = "Transportasi", type = "EXPENSE")
            ),
            onDismiss = {},
            onSave = { _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditDeleteSheetContentPreview() {
    WletTheme {
        EditDeleteSheetContent(
            transaction = Transaction(
                name = "Belanja Bulanan",
                amount = 250000.0,
                date = System.currentTimeMillis(),
                description = "Beli kebutuhan dapur",
                categoryId = 1,
                transactionType = "EXPENSE"
            ),
            categories = listOf(Category(id = 1, name = "Belanja", type = "EXPENSE")),
            onClose = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
