package com.splendid.data.local.dao

import androidx.room.*
import com.splendid.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long
    
    @Update
    suspend fun update(budget: BudgetEntity)
    
    @Query("SELECT * FROM budget WHERE month = :month AND year = :year")
    suspend fun getBudgetByMonth(month: Int, year: Int): BudgetEntity?
    
    @Query("SELECT * FROM budget WHERE month = :month AND year = :year")
    fun getBudgetByMonthFlow(month: Int, year: Int): Flow<BudgetEntity?>
    
    @Query("SELECT * FROM budget ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>
}
