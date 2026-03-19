package com.splendid.data.local.dao

import androidx.room.*
import com.splendid.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long
    
    @Update
    suspend fun update(expense: ExpenseEntity)
    
    @Delete
    suspend fun delete(expense: ExpenseEntity)
    
    @Query("SELECT * FROM expense WHERE date = :date ORDER BY timestamp DESC, id DESC")
    fun getExpensesByDate(date: Long): Flow<List<ExpenseEntity>>
    
    @Query("""
        SELECT * FROM expense 
        WHERE date >= :startDate AND date < :endDate 
        ORDER BY date DESC, timestamp DESC, id DESC
    """)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expense WHERE categoryId = :categoryId")
    fun getExpensesByCategory(categoryId: Int): Flow<List<ExpenseEntity>>
    
    @Query("""
        SELECT DISTINCT title
        FROM expense 
        WHERE LOWER(title) LIKE LOWER(:query || '%')
        GROUP BY LOWER(title)
        ORDER BY COUNT(*) DESC 
        LIMIT 5
    """)
    suspend fun getSmartSuggestions(query: String): List<TitleSuggestion>
    
    @Query("""
        SELECT SUM(amount * quantity) 
        FROM expense 
        WHERE date >= :monthStart AND date < :monthEnd
    """)
    suspend fun getMonthlyTotal(monthStart: Long, monthEnd: Long): Double?
    
    @Query("""
        SELECT date, SUM(amount * quantity) as total 
        FROM expense 
        WHERE date >= :startDate AND date < :endDate 
        GROUP BY date 
        ORDER BY date
    """)
    suspend fun getDailyTotals(startDate: Long, endDate: Long): List<DailyTotal>
    
    @Query("""
        SELECT 
            LOWER(title) as normalizedTitle,
            MAX(title) as displayTitle,
            SUM(amount * quantity) as total 
        FROM expense 
        WHERE date >= :startDate AND date < :endDate
        GROUP BY LOWER(title)
        ORDER BY total DESC
        LIMIT 10
    """)
    suspend fun getSpendingByTitle(startDate: Long, endDate: Long): List<TitleTotal>
}

data class DailyTotal(
    val date: Long,
    val total: Double
)

data class TitleTotal(
    val normalizedTitle: String,
    val displayTitle: String,
    val total: Double
)

data class TitleSuggestion(
    val title: String
)
