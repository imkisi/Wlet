package com.example.wlet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wlet.R
import com.example.wlet.ui.theme.WletTheme

@Composable
fun SettingsScreenContent() {
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
        SettingsScreenContent()
    }
}
