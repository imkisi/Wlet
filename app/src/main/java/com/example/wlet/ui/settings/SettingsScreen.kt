package com.example.wlet.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wlet.R
import com.example.wlet.data.local.entities.Category
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.theme.RobotoMono
import com.example.wlet.ui.theme.WletTheme

/**
 * Main content for the Settings Screen.
 * Handled similarly to DashboardScreenContent for consistency.
 */
@Composable
fun SettingsScreenContent(viewModel: FinanceViewModel) {
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    SettingsScreenUI(
        categories = categories,
        onAddCategory = { name, type ->
            viewModel.insertCategory(Category(name = name, type = type))
        },
        onDeleteCategory = { category ->
            viewModel.deleteCategory(category)
        }
    )
}

@Composable
fun SettingsScreenUI(
    categories: List<Category>,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    var isManageCategoriesOpen by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0ECE9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Reusable Profile Component
            ProfileCreditElement()

            Spacer(modifier = Modifier.height(32.dp))

            // Grouped Settings List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    iconRes = R.drawable.categories,
                    title = "Kelola Kategori",
                    subtitle = "Tambah atau hapus kategori",
                    onClick = { isManageCategoriesOpen = true }
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (isManageCategoriesOpen) {
            ManageCategoriesDialog(
                categories = categories,
                onAddCategory = onAddCategory,
                onDeleteCategory = onDeleteCategory,
                onDismiss = { isManageCategoriesOpen = false }
            )
        }
    }
}

@Composable
fun ProfileCreditElement() {
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "kisi labs" header capsule
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("kisi") }
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)) { append("labs") }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    fontSize = 20.sp,
                    fontFamily = RobotoMono
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Blue Avatar Placeholder
            Box(modifier = Modifier.size(100.dp).background(Color.Blue, CircleShape))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "© 2026 kisiLabs",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ARTIST WHO DESIGN AND\nCODE",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic Social Buttons with staggered layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                SocialIconButton("PORTFOLIO", iconRes = R.drawable.link) { uriHandler.openUri("https://kisilabs.space") }

                // Staggered (Lowered) middle button
                SocialIconButton(label = "GITHUB", iconRes = R.drawable.link, modifier = Modifier.padding(top = 10.dp)) { uriHandler.openUri("https://github.com/imkisi") }

                SocialIconButton("LINKEDIN", iconRes = R.drawable.link) { uriHandler.openUri("https://linkedin.com/in/bagas-d") }
            }
        }
    }
}

@Composable
fun SocialIconButton(
    label: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.size(54.dp).clickable { onClick() },
            shape = CircleShape,
            color = Color(0xFFE0E0E0).copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(id = iconRes), contentDescription = label, modifier = Modifier.size(24.dp), tint = Color.DarkGray)
            }
        }
    }
}

@Composable
fun ManageCategoriesDialog(
    categories: List<Category>,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Kelola Kategori", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TabChip(selected = selectedType == "EXPENSE", text = "Pengeluaran") { selectedType = "EXPENSE" }
                    TabChip(selected = selectedType == "INCOME", text = "Pemasukan") { selectedType = "INCOME" }
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nama Kategori") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategory(newCategoryName, selectedType)
                            newCategoryName = ""
                        }
                    }) {
                        Icon(painter = painterResource(id = R.drawable.add), contentDescription = "Add", tint = Color.Blue)
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
                    items(categories.filter { it.type == selectedType }) { category ->
                        ListItem(
                            headlineContent = { Text(category.name) },
                            trailingContent = {
                                IconButton(onClick = { onDeleteCategory(category) }) {
                                    Icon(painter = painterResource(id = R.drawable.delete), contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Telesai", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TabChip(selected: Boolean, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Color.Blue else Color.Transparent,
        shape = CircleShape,
        border = if (selected) null else BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.height(40.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(text = text, color = if (selected) Color.White else Color.Gray, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SettingsItem(iconRes: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Blue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WletTheme {
        SettingsScreenUI(
            categories = listOf(Category(name = "Makan", type = "EXPENSE")),
            onAddCategory = { _, _ -> },
            onDeleteCategory = { }
        )
    }
}