package com.example.nguyencongnhathuy_29214654999_cr424s.data

data class TransactionEntity(
    val id: Long,
    val amount: Double,
    val type: String,
    val category: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class BudgetEntity(
    val category: String,
    val limitAmount: Double
)
