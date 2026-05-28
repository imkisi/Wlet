package com.example.wlet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wlet.data.SettingsManager
import com.example.wlet.data.local.entities.Category
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.data.repository.FinanceRepository
import com.example.wlet.ui.widget.updateWletWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for all screens.
 * Manages transactions, categories, and application settings.
 */
class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    val allTransactions: Flow<List<Transaction>> = repository.allTransactions
    val allCategories: Flow<List<Category>> = repository.allCategories

    private val _currency = MutableStateFlow(settingsManager.currency)
    val currency: StateFlow<String> = _currency

    private val _language = MutableStateFlow(settingsManager.language)
    val language: StateFlow<String> = _language

    /**
     * Helper to trigger Widget update
     */
    private fun triggerWidgetUpdate() {
        viewModelScope.launch {
            updateWletWidget(getApplication())
        }
    }

    /**
     * Inserts a new transaction into the database.
     */
    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.addTransaction(transaction)
        triggerWidgetUpdate()
    }

    /**
     * Updates an existing transaction.
     */
    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
        triggerWidgetUpdate()
    }

    /**
     * Deletes a specific transaction.
     */
    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
        triggerWidgetUpdate()
    }

    /**
     * Updates the application currency and clears transaction history.
     */
    fun updateCurrency(newCurrency: String) = viewModelScope.launch {
        repository.deleteAllTransactions()
        settingsManager.currency = newCurrency
        _currency.value = newCurrency
        triggerWidgetUpdate()
    }

    /**
     * Updates the application language.
     */
    fun updateLanguage(newLanguage: String) {
        settingsManager.language = newLanguage
        _language.value = newLanguage
    }

    /**
     * Deletes all transactions.
     */
    fun deleteAllTransactions() = viewModelScope.launch {
        repository.deleteAllTransactions()
        triggerWidgetUpdate()
    }

    /**
     * Adds a new category.
     */
    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.addCategory(category)
    }

    /**
     * Deletes a category.
     */
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    /**
     * Returns an existing category ID or creates a new one if not found.
     */
    suspend fun getOrCreateCategory(name: String, type: String): Long {
        val existing = repository.getCategoryByNameAndType(name, type)
        return existing?.id ?: repository.addCategory(Category(name = name, type = type))
    }
}

class FinanceViewModelFactory(
    private val application: Application,
    private val repository: FinanceRepository,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}