package com.splendid.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.splendid.data.local.database.SplendidDatabase
import com.splendid.data.repository.CategoryRepository
import com.splendid.data.repository.ExpenseRepository
import com.splendid.domain.model.Category
import com.splendid.domain.model.Expense
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val dailyTotals: Map<LocalDate, Double> = emptyMap(),
    val selectedDateExpenses: List<Expense> = emptyList(),
    val selectedDateTotal: Double = 0.0
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SplendidDatabase.getDatabase(application)
    private val expenseRepository = ExpenseRepository(database.expenseDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val startOfMonth = currentState.currentMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = currentState.currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            combine(
                expenseRepository.getExpensesByDateRange(startOfMonth, endOfMonth),
                categoryRepository.getAllCategories()
            ) { expenses: List<Expense>, categories: List<Category> ->
                Pair(expenses, categories)
            }.collect { (expenses, categories) ->
                val state = _uiState.value
                val dailyTotals = calculateDailyTotals(expenses, state.currentMonth)
                val selectedDateExpenses = getExpensesForDate(expenses, state.selectedDate)
                val selectedDateTotal = selectedDateExpenses.sumOf { it.total }
                
                _uiState.value = state.copy(
                    expenses = expenses,
                    categories = categories,
                    dailyTotals = dailyTotals,
                    selectedDateExpenses = selectedDateExpenses,
                    selectedDateTotal = selectedDateTotal
                )
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        val currentState = _uiState.value
        
        // Check if we need to load data for a different month
        val selectedMonth = YearMonth.from(date)
        if (selectedMonth != currentState.currentMonth) {
            // Update month and reload data
            _uiState.value = currentState.copy(
                currentMonth = selectedMonth,
                selectedDate = date
            )
            
            viewModelScope.launch {
                val startOfMonth = selectedMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = selectedMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                expenseRepository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenses ->
                    val dailyTotals = calculateDailyTotals(expenses, selectedMonth)
                    val selectedDateExpenses = getExpensesForDate(expenses, date)
                    val selectedDateTotal = selectedDateExpenses.sumOf { it.total }
                    
                    _uiState.value = _uiState.value.copy(
                        expenses = expenses,
                        dailyTotals = dailyTotals,
                        selectedDateExpenses = selectedDateExpenses,
                        selectedDateTotal = selectedDateTotal
                    )
                }
            }
        } else {
            // Same month, just update selected date
            val selectedDateExpenses = getExpensesForDate(currentState.expenses, date)
            val selectedDateTotal = selectedDateExpenses.sumOf { it.total }
            
            _uiState.value = currentState.copy(
                selectedDate = date,
                selectedDateExpenses = selectedDateExpenses,
                selectedDateTotal = selectedDateTotal
            )
        }
    }
    
    fun navigateMonth(offset: Int) {
        val currentState = _uiState.value
        val newMonth = currentState.currentMonth.plusMonths(offset.toLong())
        
        _uiState.value = currentState.copy(
            currentMonth = newMonth
        )
        
        // Reload data for new month
        viewModelScope.launch {
            val startOfMonth = newMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = newMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            expenseRepository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenses ->
                val dailyTotals = calculateDailyTotals(expenses, newMonth)
                val selectedDateExpenses = getExpensesForDate(expenses, _uiState.value.selectedDate)
                val selectedDateTotal = selectedDateExpenses.sumOf { it.total }
                
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    dailyTotals = dailyTotals,
                    selectedDateExpenses = selectedDateExpenses,
                    selectedDateTotal = selectedDateTotal
                )
            }
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
    
    private fun calculateDailyTotals(expenses: List<Expense>, month: YearMonth): Map<LocalDate, Double> {
        val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return expenses
            .filter { it.date in startOfMonth..endOfMonth }
            .groupBy { expense ->
                // Convert epoch millis to LocalDate properly
                Instant.ofEpochMilli(expense.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .mapValues { (_, expenseList: List<Expense>) -> 
                expenseList.sumOf { it.total }
            }
    }
    
    private fun getExpensesForDate(expenses: List<Expense>, date: LocalDate): List<Expense> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return expenses.filter { it.date in startOfDay..endOfDay }
    }
}
