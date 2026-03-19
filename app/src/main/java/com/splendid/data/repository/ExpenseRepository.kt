package com.splendid.data.repository

import com.splendid.data.local.dao.ExpenseDao
import com.splendid.data.local.entity.ExpenseEntity
import com.splendid.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    
    fun getExpensesByDate(date: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByDate(date).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun addExpense(expense: Expense) {
        expenseDao.insert(expense.toEntity())
    }
    
    suspend fun updateExpense(expense: Expense) {
        expenseDao.update(expense.toEntity().copy(isEdited = true))
    }
    
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense.toEntity())
    }
    
    suspend fun getSmartSuggestions(query: String): List<String> {
        return expenseDao.getSmartSuggestions(query).map { it.title }
    }
    
    suspend fun getMonthlyTotal(monthStart: Long, monthEnd: Long): Double {
        return expenseDao.getMonthlyTotal(monthStart, monthEnd) ?: 0.0
    }
    
    suspend fun getDailyTotals(startDate: Long, endDate: Long) =
        expenseDao.getDailyTotals(startDate, endDate)
    
    suspend fun getSpendingByTitle(startDate: Long, endDate: Long) =
        expenseDao.getSpendingByTitle(startDate, endDate)
    
    private fun ExpenseEntity.toDomainModel() = Expense(
        id = id,
        title = title,
        amount = amount,
        quantity = quantity,
        categoryId = categoryId,
        date = date,
        timestamp = timestamp,
        isEdited = isEdited
    )
    
    private fun Expense.toEntity() = ExpenseEntity(
        id = id,
        title = title,
        amount = amount,
        quantity = quantity,
        categoryId = categoryId,
        date = date,
        timestamp = timestamp,
        isEdited = isEdited
    )
}
