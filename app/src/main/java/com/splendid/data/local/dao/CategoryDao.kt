package com.splendid.data.local.dao

import androidx.room.*
import com.splendid.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Insert
    suspend fun insert(category: CategoryEntity): Long
    
    @Update
    suspend fun update(category: CategoryEntity)
    
    @Delete
    suspend fun delete(category: CategoryEntity)
    
    @Query("SELECT * FROM category ORDER BY sortOrder")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?
    
    @Query("SELECT COUNT(*) FROM expense WHERE categoryId = :categoryId")
    suspend fun getCategoryUsageCount(categoryId: Int): Int
    
    @Query("SELECT * FROM category WHERE isDefault = 1")
    suspend fun getDefaultCategories(): List<CategoryEntity>
}
