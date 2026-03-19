package com.splendid.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.splendid.data.local.database.SplendidDatabase
import com.splendid.data.repository.CategoryRepository
import com.splendid.domain.model.Category
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SplendidDatabase.getDatabase(application)
    private val categoryRepository = CategoryRepository(database.categoryDao())
    
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories.sortedBy { it.sortOrder },
                        isLoading = false
                    )
                }
        }
    }
    
    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                val maxSortOrder = _uiState.value.categories.maxOfOrNull { it.sortOrder } ?: 0
                categoryRepository.addCategory(
                    category.copy(sortOrder = maxSortOrder + 1)
                )
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add category: ${e.message}"
                )
            }
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(category)
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to delete category"
                )
            }
        }
    }
}
