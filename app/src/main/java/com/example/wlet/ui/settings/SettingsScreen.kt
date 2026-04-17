package com.example.wlet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.wlet.R
import com.example.wlet.data.local.entities.Category
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.theme.WletTheme

@Composable
fun SettingsScreenContent(viewModel: FinanceViewModel) {
    var isManageCategoriesOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0ECE9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                "Pengaturan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsItem(
                iconRes = R.drawable.language,
                title = "Bahasa",
                subtitle = "Bahasa Indonesia",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                iconRes = R.drawable.currency,
                title = "Mata Uang",
                subtitle = "IDR (Rp)",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                iconRes = R.drawable.palette,
                title = "Tema",
                subtitle = "Sistem",
                onClick = { /* TODO */ }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
            
            SettingsItem(
                iconRes = R.drawable.categories,
                title = "Kelola Kategori",
                subtitle = "Tambah atau hapus kategori",
                onClick = { isManageCategoriesOpen = true }
            )
        }

        if (isManageCategoriesOpen) {
            ManageCategoriesDialog(
                viewModel = viewModel,
                onDismiss = { isManageCategoriesOpen = false }
            )
        }
    }
}

@Composable
fun ManageCategoriesDialog(viewModel: FinanceViewModel, onDismiss: () -> Unit) {
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    var newCategoryName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Kelola Kategori", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == "EXPENSE",
                        onClick = { selectedType = "EXPENSE" },
                        label = { Text("Pengeluaran") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == "INCOME",
                        onClick = { selectedType = "INCOME" },
                        label = { Text("Pemasukan") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Kategori Baru") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.insertCategory(Category(name = newCategoryName, type = selectedType))
                            newCategoryName = ""
                        }
                    }) {
                        Icon(painter = painterResource(id = R.drawable.add), contentDescription = "Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(categories.filter { it.type == selectedType }) { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category.name)
                            IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                Icon(painter = painterResource(id = R.drawable.delete), contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Blue), shape = RoundedCornerShape(12.dp)) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Blue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WletTheme {
        // SettingsScreenContent requires a ViewModel, so we can't easily preview it without a mock
    }
}
