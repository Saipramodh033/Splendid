package com.splendid.data.repository

import com.splendid.data.local.dao.CategoryDao
import com.splendid.data.local.entity.CategoryEntity
import com.splendid.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun addCategory(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }
    
    suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
    }
    
    suspend fun deleteCategory(category: Category): Result<Unit> {
        // Check if category is default
        if (category.isDefault) {
            return Result.failure(Exception("Cannot delete default category"))
        }
        
        // Check if category is in use
        val usageCount = categoryDao.getCategoryUsageCount(category.id)
        if (usageCount > 0) {
            return Result.failure(Exception("Cannot delete category with expenses"))
        }
        
        categoryDao.delete(category.toEntity())
        return Result.success(Unit)
    }
    
    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)?.toDomainModel()
    }
    
    private fun CategoryEntity.toDomainModel() = Category(
        id = id,
        name = name,
        colorHex = colorHex,
        isDefault = isDefault,
        sortOrder = sortOrder
    )
    
    private fun Category.toEntity() = CategoryEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        isDefault = isDefault,
        sortOrder = sortOrder
    )
}
