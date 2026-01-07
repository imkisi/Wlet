package com.example.wlet.data.repository

import com.example.wlet.data.local.dao.TransactionDao
import com.example.wlet.data.local.entities.Category
import com.example.wlet.data.local.entities.Transaction

class FinanceRepository(private val dao: TransactionDao) {
    val allTransactions = dao.getAllTransactions()
    val allCategories = dao.getAllCategories()

    suspend fun addTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun addCategory(category: Category) {
        dao.insertCategory(category)
    }

}