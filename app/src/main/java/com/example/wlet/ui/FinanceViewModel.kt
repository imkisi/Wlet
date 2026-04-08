package com.example.wlet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wlet.data.local.entities.Category
import com.example.wlet.data.local.entities.Transaction
import com.example.wlet.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    val allTransactions: Flow<List<Transaction>> = repository.allTransactions
    val allCategories: Flow<List<Category>> = repository.allCategories

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.addTransaction(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.addCategory(category)
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}