package com.example.wlet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wlet.ui.theme.WletTheme
import java.text.SimpleDateFormat
import java.util.*

// Data Models
data class Transaksi(
    val judul: String,
    val kategori: String,
    val nominal: String,
    val tipe: String // "MASUK" or "KELUAR"
)

data class GrupTransaksi(
    val tanggal: String,
    val totalMasuk: String,
    val totalKeluar: String,
    val daftarTransaksi: List<Transaksi>
)

// Sample Data
val sampleDataGrup = listOf(
    GrupTransaksi(
        tanggal = "02 Jan",
        totalMasuk = "2.800.000",
        totalKeluar = "67.755",
        daftarTransaksi = listOf(
            Transaksi("Gaji Januari", "Pekerjaan", "2.800.000", "MASUK"),
            Transaksi("Makan Siang", "Makanan", "35.000", "KELUAR"),
            Transaksi("Parkir", "Transportasi", "2.000", "KELUAR")
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val sheetState = rememberModalBottomSheetState()
    var selectedTransaksi by remember { mutableStateOf<Transaksi?>(null) }
    var isSheetVisible by remember { mutableStateOf(false) }

    // Real-time date formatted in Indonesian
    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
    }

    Scaffold(
        containerColor = Color(0xFFF0ECE9),
        floatingActionButton = {
            FloatingDock(
                onSettingsClick = { /* navController.navigate("settings") */ },
                onAddClick = { /* Action to add new entry */ }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                HeaderSection(
                    tanggal = currentDate,
                    totalSaldo = "+Rp2.732.245",
                    onDateClick = { /* navController.navigate("monthly_report") */ }
                )
            }

            items(sampleDataGrup) { grup ->
                DailyTransactionCard(
                    grup = grup,
                    onItemClick = { transaksi ->
                        selectedTransaksi = transaksi
                        isSheetVisible = true
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (isSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isSheetVisible = false },
                sheetState = sheetState
            ) {
                EditDeleteSheetContent(
                    transaksi = selectedTransaksi,
                    onClose = { isSheetVisible = false }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(tanggal: String, totalSaldo: String, onDateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
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

        Spacer(modifier = Modifier.height(12.dp))
        Text("Ringkasan Bulanan", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = totalSaldo,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
    }
}

@Composable
fun DailyTransactionCard(grup: GrupTransaksi, onItemClick: (Transaksi) -> Unit) {
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
                Text(text = grup.tanggal, fontWeight = FontWeight.Bold)
                Row {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Masuk", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Text("Rp${grup.totalMasuk}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Keluar", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        Text("Rp${grup.totalKeluar}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            grup.daftarTransaksi.forEach { transaksi ->
                TransactionRow(transaksi, onClick = { onItemClick(transaksi) })
            }
        }
    }
}

@Composable
fun TransactionRow(item: Transaksi, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.judul, style = MaterialTheme.typography.bodyLarge)
            Text(item.kategori, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }

        val colorScheme = if (item.tipe == "MASUK") Color.Blue else Color.Red
        val bgColor = colorScheme.copy(alpha = 0.1f)

        Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = (if (item.tipe == "MASUK") "+" else "-") + "Rp${item.nominal}",
                color = colorScheme,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun FloatingDock(onSettingsClick: () -> Unit, onAddClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Surface(
            shape = CircleShape,
            shadowElevation = 6.dp,
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                IconButton(onClick = { /* Home Action */ }) {
                    Icon(Icons.Default.Home, contentDescription = null)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        FloatingActionButton(
            onClick = onAddClick,
            shape = CircleShape,
            containerColor = Color.Blue,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EditDeleteSheetContent(transaksi: Transaksi?, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Opsi Transaksi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Transaksi: ${transaksi?.judul ?: ""}")
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            Text("Tutup")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    WletTheme {
        val navController = rememberNavController()
        HomeScreen(navController)
    }
}