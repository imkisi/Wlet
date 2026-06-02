package com.example.wlet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.res.stringResource
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.ui.FinanceViewModel
import com.example.wlet.ui.FinanceViewModelFactory
import com.example.wlet.ui.dashboard.DashboardScreenContent
import com.example.wlet.ui.home.AddEditTransactionSheetContent
import com.example.wlet.ui.home.FloatingDock
import com.example.wlet.ui.home.HomeScreenContent
import com.example.wlet.ui.settings.SettingsScreenContent
import com.example.wlet.ui.theme.WletTheme
import kotlinx.coroutines.launch

/**
 * MainActivity is the primary entry point of the application.
 * It manages the app's root navigation structure using a HorizontalPager and coordinates
 * global UI elements like the Transaction entry Bottom Sheet.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: FinanceViewModel by viewModels {
        val app = application as WletApplication
        FinanceViewModelFactory(app, app.repository, app.settingsManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Force Day (Light) mode globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        
        // Restore application locale from settings
        val app = application as WletApplication
        val savedLang = app.settingsManager.language
        val appLocale = LocaleListCompat.forLanguageTags(savedLang)
        if (AppCompatDelegate.getApplicationLocales() != appLocale) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        
        enableEdgeToEdge()
        setContent {
            WletTheme {
                MainContainer(viewModel, intent)
            }
        }
    }

    /**
     * Responds to new intents (e.g., from widgets) while the activity is in the foreground.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

/**
 * MainContainer manages the navigation and shared states like Bottom Sheets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(viewModel: FinanceViewModel, intent: Intent?) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    // Shared Bottom Sheet State for Adding/Editing Transactions
    val addEditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddEditSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    // Check for Widget Action
    LaunchedEffect(intent) {
        if (intent?.getBooleanExtra("SHOW_ADD_SHEET", false) == true) {
            transactionToEdit = null
            showAddEditSheet = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0ECE9))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> HomeScreenContent(
                    viewModel = viewModel,
                    onShowEditSheet = { transaction ->
                        transactionToEdit = transaction
                        showAddEditSheet = true
                    }
                )
                1 -> DashboardScreenContent(viewModel)
                2 -> SettingsScreenContent(viewModel)
            }
        }

        // Action Buttons (Fixed at bottom center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            FloatingDock(
                onHomeClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                },
                onDashboardClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                },
                onSettingsClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                },
                onAddClick = {
                    transactionToEdit = null
                    showAddEditSheet = true
                }
            )
        }

        // Modal Bottom Sheet for Transaction Input
        if (showAddEditSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddEditSheet = false },
                sheetState = addEditSheetState,
                containerColor = Color(0xFFF0ECE9)
            ) {
                AddEditTransactionSheetContent(
                    title = if (transactionToEdit == null) stringResource(R.string.add_transaction) else stringResource(R.string.edit_transaction),
                    transaction = transactionToEdit,
                    categories = categories,
                    onSave = { name, amount, desc, categoryName, type ->
                        coroutineScope.launch {
                            val categoryId = viewModel.getOrCreateCategory(categoryName, type)
                            if (transactionToEdit == null) {
                                viewModel.insert(
                                    Transaction(
                                        name = name,
                                        amount = amount,
                                        date = System.currentTimeMillis(),
                                        description = desc,
                                        categoryId = categoryId,
                                        transactionType = type
                                    )
                                )
                            } else {
                                viewModel.update(
                                    transactionToEdit!!.copy(
                                        name = name,
                                        amount = amount,
                                        description = desc,
                                        categoryId = categoryId,
                                        transactionType = type
                                    )
                                )
                            }
                            showAddEditSheet = false
                        }
                    }
                )
            }
        }
    }
}
