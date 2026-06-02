package com.example.wlet.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
            containerColor = Color(0xFFF0ECE9),
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
                val currencyDisplay = when (currentCurrency) {
                    "USD" -> stringResource(R.string.currency_usd)
                    "EUR" -> stringResource(R.string.currency_eur)
                    "JPY" -> stringResource(R.string.currency_jpy)
                    "GBP" -> stringResource(R.string.currency_gbp)
                    else -> stringResource(R.string.currency_idr)
                }
                SettingsItem(
                    iconRes = R.drawable.currency,
                    title = stringResource(R.string.currency),
                    subtitle = currencyDisplay,
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
    Column(modifier = Modifier
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.language),
                contentDescription = null,
                tint = Color.Blue,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.select_language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
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
    Surface(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFFE0E0E0).copy(alpha = 0.7f))
    {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
                .clickable { onClick() }
                .padding(vertical = 0.dp, horizontal = 8.dp),
            headlineContent = {
                Text(label,
                fontFamily = RobotoMono,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
            trailingContent = {
                if (selected)
                    RadioButton(selected = true,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Color.Blue)
                    )
            }
        )
    }
}

@Composable
fun CurrencyChangeSheetContent(currentCurrency: String, onConfirm: (String) -> Unit) {
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    val currencies = listOf(
        "IDR" to R.string.currency_idr,
        "USD" to R.string.currency_usd,
        "EUR" to R.string.currency_eur,
        "JPY" to R.string.currency_jpy,
        "GBP" to R.string.currency_gbp
    )

    Column(modifier = Modifier
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.currency),
                contentDescription = null,
                tint = Color.Blue,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.change_currency), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.currency_warning),
            fontFamily = RobotoMono,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        currencies.forEach { (code, resId) ->
            val currencyLabel = stringResource(resId)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(50.dp),
                color = Color(0xFFE0E0E0).copy(alpha = 0.7f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCurrency = code }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currencyLabel, modifier = Modifier.padding(start = 16.dp), fontFamily = RobotoMono)
                    RadioButton(
                        selected = selectedCurrency == code,
                        onClick = { selectedCurrency = code },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Blue,
                            unselectedColor = Color.Blue.copy(alpha = 0.8f))
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onConfirm(selectedCurrency) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
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

    Column(modifier = Modifier
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.categories),
                contentDescription = null,
                tint = Color.Blue,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.manage_categories), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontFamily = RobotoMono)
        Spacer(modifier = Modifier.height(18.dp))
        // Main Pill-shaped Container Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            color = Color(0xFFE0E0E0).copy(alpha = 0.7f)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionTypeSlider(
                    selected = selectedType == "EXPENSE",
                    text = stringResource(R.string.expense),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = "EXPENSE" }
                )
                TransactionTypeSlider(
                    selected = selectedType == "INCOME",
                    text = stringResource(R.string.income),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = "INCOME" }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            items(categories.filter { it.type == selectedType }) { category ->
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE0DCD9).copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = RobotoMono,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Black
                        )

                        IconButton(
                            onClick = { onDeleteCategory(category) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Delete Category",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionTypeSlider(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) Color.Blue else Color.Transparent,
        contentColor = if (selected) Color.White else Color.Blue
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = RobotoMono
            )
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
