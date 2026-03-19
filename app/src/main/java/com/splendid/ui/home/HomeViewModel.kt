package com.splendid.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.splendid.data.local.database.SplendidDatabase
import com.splendid.data.repository.BudgetRepository
import com.splendid.data.repository.CategoryRepository
import com.splendid.data.repository.ExpenseRepository
import com.splendid.domain.model.Budget
import com.splendid.domain.model.BudgetStatus
import com.splendid.domain.model.Category
import com.splendid.domain.model.Expense
import com.splendid.utils.DateUtils
import com.splendid.utils.DateUtils.getCurrentMonth
import com.splendid.utils.DateUtils.getCurrentYear
import com.splendid.utils.DateUtils.getMonthEnd
import com.splendid.utils.DateUtils.getMonthStart
import com.splendid.utils.DateUtils.getTodayMidnight
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedDate: Long = getTodayMidnight(),
    val expenses: List<Expense> = emptyList(),
    val monthlyExpenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val dailyTotal: Double = 0.0,
    val budgetStatus: BudgetStatus? = null,
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SplendidDatabase.getDatabase(application)
    private val expenseRepository = ExpenseRepository(database.expenseDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    private val budgetRepository = BudgetRepository(database.budgetDao())
    
    private val _selectedDate = MutableStateFlow(getTodayMidnight())
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            val currentMonth = getCurrentMonth()
            val currentYear = getCurrentYear()
            val monthStart = getMonthStart(currentMonth, currentYear)
            val monthEnd = getMonthEnd(currentMonth, currentYear)
            
            // Combine flows
            combine(
                _selectedDate,
                expenseRepository.getExpensesByDate(_selectedDate.value),
                expenseRepository.getExpensesByDateRange(monthStart, monthEnd),
                categoryRepository.getAllCategories(),
                budgetRepository.getBudgetByMonth(currentMonth, currentYear)
            ) { date, dailyExpenses, monthlyExpenses, categories, budget ->
                // Enrich daily expenses with category info
                val enrichedDailyExpenses = dailyExpenses.map { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    expense.copy(
                        categoryName = category?.name ?: "",
                        categoryColor = category?.colorHex ?: ""
                    )
                }
                
                // Enrich monthly expenses with category info
                val enrichedMonthlyExpenses = monthlyExpenses.map { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    expense.copy(
                        categoryName = category?.name ?: "",
                        categoryColor = category?.colorHex ?: ""
                    )
                }
                
                // Calculate daily total
                val dailyTotal = enrichedDailyExpenses.sumOf { it.total }
                
                // Calculate budget status
                val budgetStatus = budget?.let { calculateBudgetStatus(it) }
                
                HomeUiState(
                    selectedDate = date,
                    expenses = enrichedDailyExpenses,
                    monthlyExpenses = enrichedMonthlyExpenses,
                    categories = categories,
                    dailyTotal = dailyTotal,
                    budgetStatus = budgetStatus,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    private suspend fun calculateBudgetStatus(budget: Budget): BudgetStatus {
        val monthStart = getMonthStart(budget.month, budget.year)
        val monthEnd = getMonthEnd(budget.month, budget.year)
        val currentSpending = expenseRepository.getMonthlyTotal(monthStart, monthEnd)
        
        return BudgetStatus(
            budget = budget,
            currentSpending = currentSpending,
            isOverBudget = currentSpending > budget.limitAmount,
            remainingAmount = budget.limitAmount - currentSpending
        )
    }
    
    fun selectDate(date: Long) {
        _selectedDate.value = date
        viewModelScope.launch {
            val expenses = expenseRepository.getExpensesByDate(date).first()
            val categories = categoryRepository.getAllCategories().first()
            
            val enrichedExpenses = expenses.map { expense ->
                val category = categories.find { it.id == expense.categoryId }
                expense.copy(
                    categoryName = category?.name ?: "",
                    categoryColor = category?.colorHex ?: ""
                )
            }
            
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                expenses = enrichedExpenses,
                dailyTotal = enrichedExpenses.sumOf { it.total }
            )
        }
    }
    
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
        }
    }
    
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }
    
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }
    
    suspend fun getSmartSuggestions(query: String): List<String> {
        return if (query.length >= 2) {
            expenseRepository.getSmartSuggestions(query)
        } else {
            emptyList()
        }
    }
}
