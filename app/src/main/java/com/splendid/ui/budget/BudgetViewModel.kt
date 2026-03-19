package com.splendid.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.splendid.data.local.database.SplendidDatabase
import com.splendid.data.repository.BudgetRepository
import com.splendid.domain.model.Budget
import com.splendid.utils.DateUtils.getCurrentMonth
import com.splendid.utils.DateUtils.getCurrentYear
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BudgetUiState(
    val currentBudget: Budget? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SplendidDatabase.getDatabase(application)
    private val budgetRepository = BudgetRepository(database.budgetDao())
    
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentBudget()
    }
    
    private fun loadCurrentBudget() {
        viewModelScope.launch {
            budgetRepository.getBudgetByMonth(getCurrentMonth(), getCurrentYear())
                .collect { budget ->
                    _uiState.value = _uiState.value.copy(
                        currentBudget = budget,
                        isLoading = false
                    )
                }
        }
    }
    
    fun saveBudget(limitAmount: Double) {
        viewModelScope.launch {
            try {
                val currentBudget = _uiState.value.currentBudget
                if (currentBudget != null) {
                    // Update existing budget
                    budgetRepository.updateBudget(
                        currentBudget.copy(limitAmount = limitAmount)
                    )
                } else {
                    // Create new budget
                    budgetRepository.setBudget(
                        Budget(
                            month = getCurrentMonth(),
                            year = getCurrentYear(),
                            limitAmount = limitAmount
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save budget: ${e.message}"
                )
            }
        }
    }
}
