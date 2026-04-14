package com.example.wlet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.wlet.ui.home.AddEditTransactionDialog
import com.example.wlet.ui.home.FloatingDock
import com.example.wlet.ui.home.HomeScreenContent
import com.example.wlet.ui.settings.SettingsScreenContent
import com.example.wlet.ui.theme.WletTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels {
        FinanceViewModelFactory((application as WletApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WletTheme {
                MainContainer(viewModel)
            }
        }
    }
}

@Composable
fun MainContainer(viewModel: FinanceViewModel) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var isAddDialogOpen by remember { mutableStateOf(false) }
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0ECE9))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HomeScreenContent(viewModel)
                1 -> SettingsScreenContent()
            }
        }

        // Persistent Navigation and Action buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            FloatingDock(
                onSettingsClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                onAddClick = { isAddDialogOpen = true },
                onHomeClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            )
        }

        if (isAddDialogOpen) {
            AddEditTransactionDialog(
                title = "Tambah Transaksi",
                categories = categories,
                onDismiss = { isAddDialogOpen = false },
                onSave = { name, amount, desc, catId, type ->
                    viewModel.insert(
                        Transaction(
                            name = name,
                            amount = amount,
                            date = System.currentTimeMillis(),
                            description = desc,
                            categoryId = catId,
                            transactionType = type
                        )
                    )
                    isAddDialogOpen = false
                }
            )
        }
    }
}
