package com.example.nguyencongnhathuy_29214654999_cr424s.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nguyencongnhathuy_29214654999_cr424s.data.BudgetEntity
import com.example.nguyencongnhathuy_29214654999_cr424s.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("smart_wallet", 0)
    private val _transactions = MutableStateFlow(loadTransactions())
    private val _budgets = MutableStateFlow(loadBudgets())

    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()
    val budgets: StateFlow<List<BudgetEntity>> = _budgets.asStateFlow()

    fun addTransaction(amount: Double, type: String, category: String, note: String) {
        val updated = listOf(
            TransactionEntity(System.currentTimeMillis(), amount, type, category, note)
        ) + _transactions.value
        _transactions.value = updated
        saveTransactions(updated)
    }

    fun updateTransaction(transaction: TransactionEntity) {
        val updated = _transactions.value.map {
            if (it.id == transaction.id) transaction else it
        }
        _transactions.value = updated
        saveTransactions(updated)
    }

    fun clearAllData() {
        _transactions.value = emptyList()
        _budgets.value = emptyList()
        preferences.edit().clear().apply()
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        val updated = _transactions.value.filterNot { it.id == transaction.id }
        _transactions.value = updated
        saveTransactions(updated)
    }

    fun saveBudget(category: String, limitAmount: Double) {
        val updated = (_budgets.value.filterNot { it.category == category } +
            BudgetEntity(category, limitAmount)).sortedBy { it.category }
        _budgets.value = updated
        preferences.edit().putString(
            "budgets",
            updated.joinToString("\n") { "${it.category}|${it.limitAmount}" }
        ).apply()
    }

    private fun saveTransactions(items: List<TransactionEntity>) {
        preferences.edit().putString(
            "transactions",
            items.joinToString("\n") {
                "${it.id}|${it.amount}|${it.type}|${it.category}|${it.note.replace("|", " ")}|${it.timestamp}"
            }
        ).apply()
    }

    private fun loadTransactions(): List<TransactionEntity> =
        preferences.getString("transactions", "").orEmpty().lineSequence()
            .mapNotNull { line ->
                val parts = line.split("|", limit = 6)
                if (parts.size == 6) runCatching {
                    TransactionEntity(
                        parts[0].toLong(), parts[1].toDouble(), parts[2], parts[3], parts[4], parts[5].toLong()
                    )
                }.getOrNull() else null
            }.toList()

    private fun loadBudgets(): List<BudgetEntity> =
        preferences.getString("budgets", "").orEmpty().lineSequence()
            .mapNotNull { line ->
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) runCatching {
                    BudgetEntity(parts[0], parts[1].toDouble())
                }.getOrNull() else null
            }.toList()
}
