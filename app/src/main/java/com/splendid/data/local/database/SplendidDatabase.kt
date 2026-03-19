package com.splendid.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.splendid.data.local.dao.*
import com.splendid.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        IOUEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SplendidDatabase : RoomDatabase() {
    
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun iouDao(): IOUDao
    abstract fun budgetDao(): BudgetDao
    
    companion object {
        @Volatile
        private var INSTANCE: SplendidDatabase? = null
        
        fun getDatabase(context: Context): SplendidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SplendidDatabase::class.java,
                    "splendid_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }
        }
        
        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Food", colorHex = "#E53935", isDefault = true, sortOrder = 1),
                CategoryEntity(name = "Transport", colorHex = "#1E88E5", isDefault = true, sortOrder = 2),
                CategoryEntity(name = "Shopping", colorHex = "#8E24AA", isDefault = true, sortOrder = 3),
                CategoryEntity(name = "Entertainment", colorHex = "#FF6F00", isDefault = true, sortOrder = 4),
                CategoryEntity(name = "Bills", colorHex = "#43A047", isDefault = true, sortOrder = 5),
                CategoryEntity(name = "Health", colorHex = "#D81B60", isDefault = true, sortOrder = 6),
                CategoryEntity(name = "Other", colorHex = "#546E7A", isDefault = true, sortOrder = 7)
            )
            
            defaultCategories.forEach { category ->
                categoryDao.insert(category)
            }
        }
    }
}
