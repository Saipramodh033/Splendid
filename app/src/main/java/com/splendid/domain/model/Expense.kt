package com.splendid.domain.model

data class Expense(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val quantity: Int = 1,
    val categoryId: Int,
    val categoryName: String = "",
    val categoryColor: String = "",
    val date: Long,
    val timestamp: Long,
    val isEdited: Boolean = false
) {
    val total: Double
        get() = amount * quantity
}
