package com.splendid.data.repository

import com.splendid.data.local.dao.BudgetDao
import com.splendid.data.local.entity.BudgetEntity
import com.splendid.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(private val budgetDao: BudgetDao) {
    
    fun getBudgetByMonth(month: Int, year: Int): Flow<Budget?> {
        return budgetDao.getBudgetByMonthFlow(month, year).map { entity ->
            entity?.toDomainModel()
        }
    }
    
    suspend fun setBudget(budget: Budget): Long {
        return budgetDao.insert(budget.toEntity())
    }
    
    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget.toEntity())
    }
    
    suspend fun getBudgetByMonthSync(month: Int, year: Int): Budget? {
        return budgetDao.getBudgetByMonth(month, year)?.toDomainModel()
    }
    
    private fun BudgetEntity.toDomainModel() = Budget(
        id = id,
        month = month,
        year = year,
        limitAmount = limitAmount
    )
    
    private fun Budget.toEntity() = BudgetEntity(
        id = id,
        month = month,
        year = year,
        limitAmount = limitAmount
    )
}
