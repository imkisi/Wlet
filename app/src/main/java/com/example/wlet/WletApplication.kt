package com.example.wlet

import android.app.Application
import com.example.wlet.data.AppDatabase
import com.example.wlet.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WletApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { FinanceRepository(database.transactionDao()) }
}