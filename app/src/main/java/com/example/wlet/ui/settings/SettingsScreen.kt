package com.example.wlet.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.wlet.R
import com.example.wlet.data.local.entities.Category
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.theme.RobotoMono
import com.example.wlet.ui.theme.WletTheme

/**
 * SettingsSheetType defines the different types of bottom sheets available in the Settings screen.
 */
enum class SettingsSheetType { LANGUAGE, CURRENCY, CATEGORIES }

/**
 * SettingsScreenContent manages the state and logic for application preferences.
 * Handles language switching, currency updates with history deletion, and category management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(viewModel: FinanceViewModel) {
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    val currentCurrency by viewModel.currency.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeSheet by remember { mutableStateOf<SettingsSheetType?>(null) }

    // Render the main visual UI
    SettingsScreenUI(
        currentCurrency = currentCurrency,
        currentLanguage = currentLanguage,
        onSheetRequest = { activeSheet = it }
    )

    // Modal Bottom Sheets for individual settings
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            when (activeSheet) {
                SettingsSheetType.LANGUAGE -> LanguageSelectionSheetContent(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = { localeCode ->
                        // Persist language change and apply system-wide
                        viewModel.updateLanguage(localeCode)
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                        activeSheet = null
                    }
                )
                SettingsSheetType.CURRENCY -> CurrencyChangeSheetContent(
                    currentCurrency = currentCurrency,
                    onConfirm = { newCurrency ->
                        // Automatically changes currency and deletes existing transactions as requested
                        viewModel.updateCurrency(newCurrency)
                        activeSheet = null
                    }
                )
                SettingsSheetType.CATEGORIES -> ManageCategoriesSheetContent(
                    categories = categories,
                    onAddCategory = { name, type -> 
                        viewModel.insertCategory(Category(name = name, type = type)) 
                    },
                    onDeleteCategory = { viewModel.deleteCategory(it) }
                )
                else -> {}
            }
        }
    }
}

@Composable
fun SettingsScreenUI(
    currentCurrency: String,
    currentLanguage: String,
    onSheetRequest: (SettingsSheetType) -> Unit
) {
    val scrollState = rememberScrollState()
    val languageDisplay = remember(currentLanguage) {
        if (currentLanguage == "id") "Bahasa Indonesia" else "English"
    }

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
            Spacer(modifier = Modifier.height(80.dp))

            // User customized Profile and Branding section
            ProfileCreditElement()

            Spacer(modifier = Modifier.height(32.dp))

            // Settings list with consistent rounded surface style
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsItem(
                    iconRes = R.drawable.language,
                    title = stringResource(R.string.language),
                    subtitle = languageDisplay,
                    onClick = { onSheetRequest(SettingsSheetType.LANGUAGE) }
                )
                SettingsItem(
                    iconRes = R.drawable.currency,
                    title = stringResource(R.string.currency),
                    subtitle = currentCurrency,
                    onClick = { onSheetRequest(SettingsSheetType.CURRENCY) }
                )
                SettingsItem(
                    iconRes = R.drawable.categories,
                    title = stringResource(R.string.manage_categories),
                    subtitle = stringResource(R.string.manage_categories_desc),
                    onClick = { onSheetRequest(SettingsSheetType.CATEGORIES) }
                )
            }
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun ProfileCreditElement() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Blue)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "© 2026 kisiLabs",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontFamily = RobotoMono
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ARTIST WHO DESIGN AND\nCODE",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            fontFamily = RobotoMono
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SocialIconButton("PORTFOLIO", R.drawable.link) { uriHandler.openUri("https://kisilabs.space") }
            SocialIconButton("GITHUB", R.drawable.link, Modifier.padding(top = 10.dp)) { uriHandler.openUri("https://github.com/imkisi") }
            SocialIconButton("LINKEDIN", R.drawable.link) { uriHandler.openUri("https://linkedin.com/in/bagas-d") }
        }
    }
}

@Composable
fun SocialIconButton(label: String, iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
fun LanguageSelectionSheetContent(currentLanguage: String, onLanguageSelected: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
        Text(stringResource(R.string.select_language), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(24.dp))
        
        LanguageItem(
            label = "Bahasa Indonesia",
            selected = currentLanguage == "id",
            onClick = { onLanguageSelected("id") }
        )
        LanguageItem(
            label = "English",
            selected = currentLanguage == "en",
            onClick = { onLanguageSelected("en") }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LanguageItem(label: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(label, fontFamily = RobotoMono, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        leadingContent = { Icon(painterResource(R.drawable.language), null, tint = if (selected) Color.Blue else Color.Gray, modifier = Modifier.size(24.dp)) },
        trailingContent = { if (selected) RadioButton(selected = true, onClick = null) }
    )
}

@Composable
fun CurrencyChangeSheetContent(currentCurrency: String, onConfirm: (String) -> Unit) {
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    val currencies = listOf("IDR", "USD", "EUR", "JPY", "GBP")

    Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
        Text(stringResource(R.string.change_currency), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = Color.Red.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
        ) {
            Text(
                text = stringResource(R.string.currency_warning),
                color = Color.Red,
                fontFamily = RobotoMono,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(stringResource(R.string.select_new_currency), style = MaterialTheme.typography.labelLarge, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(8.dp))
        
        currencies.forEach { currency ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCurrency = currency }
                    .padding(vertical = 12.dp)
            ) {
                RadioButton(selected = selectedCurrency == currency, onClick = { selectedCurrency = currency })
                Text(text = currency, modifier = Modifier.padding(start = 16.dp), fontFamily = RobotoMono)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onConfirm(selectedCurrency) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.confirm_and_delete), fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ManageCategoriesSheetContent(
    categories: List<Category>,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") }

    Column(modifier = Modifier.padding(24.dp).fillMaxHeight(0.85f)) {
        Text(stringResource(R.string.manage_categories), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                modifier = Modifier.weight(1f).height(44.dp).clickable { selectedType = "EXPENSE" },
                shape = CircleShape,
                color = if (selectedType == "EXPENSE") Color.Blue else Color.LightGray.copy(alpha = 0.3f),
                contentColor = if (selectedType == "EXPENSE") Color.White else Color.Black
            ) {
                Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.expense), fontFamily = RobotoMono) }
            }
            Surface(
                modifier = Modifier.weight(1f).height(44.dp).clickable { selectedType = "INCOME" },
                shape = CircleShape,
                color = if (selectedType == "INCOME") Color.Blue else Color.LightGray.copy(alpha = 0.3f),
                contentColor = if (selectedType == "INCOME") Color.White else Color.Black
            ) {
                Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.income), fontFamily = RobotoMono) }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                label = { Text(stringResource(R.string.category_name), fontFamily = RobotoMono) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = RobotoMono)
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
                    headlineContent = { Text(category.name, fontFamily = RobotoMono) },
                    trailingContent = {
                        IconButton(onClick = { onDeleteCategory(category) }) {
                            Icon(painter = painterResource(id = R.drawable.delete), contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
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
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = RobotoMono)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WletTheme {
        SettingsScreenUI(
            currentCurrency = "IDR",
            currentLanguage = "id",
            onSheetRequest = {}
        )
    }
}
