package com.splendid.domain.model

data class Budget(
    val id: Int = 0,
    val month: Int,
    val year: Int,
    val limitAmount: Double
)

data class BudgetStatus(
    val budget: Budget?,
    val currentSpending: Double,
    val isOverBudget: Boolean,
    val remainingAmount: Double
)
