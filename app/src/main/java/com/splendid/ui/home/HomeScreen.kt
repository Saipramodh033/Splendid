package com.splendid.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.splendid.domain.model.Expense
import com.splendid.ui.components.*
import com.splendid.ui.theme.*
import com.splendid.utils.CurrencyUtils.toCurrencyString
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenses = uiState.expenses
    
    // Group expenses by category
    val groupedExpenses = expenses.groupBy { it.categoryName }
    var expandedCategories by remember { mutableStateOf(groupedExpenses.keys.toSet()) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Today's Expenses",
                                color = TextOnGradient,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
                                    .format(Date()),
                                color = TextOnGradient.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ButtonOnGradient,
                    contentColor = ButtonTextOnGradient
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Budget warning banner
                uiState.budgetStatus?.let { status ->
                    if (status.isOverBudget) {
                        item {
                            BudgetWarningBanner(
                                spent = status.currentSpending,
                                limit = status.budget?.limitAmount ?: 0.0
                            )
                        }
                    }
                }

                // Today's total card
                item {
                    GradientCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Today's Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnGradient
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.dailyTotal.toCurrencyString(),
                                style = MaterialTheme.typography.displaySmall,
                                color = TextOnGradient
                            )
                        }
                    }
                }

                // Category-wise grouped expenses
                if (groupedExpenses.isEmpty()) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No expenses today",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to add your first expense",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    groupedExpenses.forEach { (categoryName, categoryExpenses) ->
                        item(key = categoryName) {
                            CategorySection(
                                categoryName = categoryName,
                                categoryColor = categoryExpenses.firstOrNull()?.categoryColor ?: "#546E7A",
                                expenses = categoryExpenses,
                                total = categoryExpenses.sumOf { it.total },
                                isExpanded = expandedCategories.contains(categoryName),
                                onToggleExpand = {
                                    expandedCategories = if (expandedCategories.contains(categoryName)) {
                                        expandedCategories - categoryName
                                    } else {
                                        expandedCategories + categoryName
                                    }
                                },
                                onEdit = { expense -> expenseToEdit = expense },
                                onDelete = { expense -> expenseToDelete = expense }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddExpenseDialog(
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onSave = { expense ->
                viewModel.addExpense(expense)
                showAddDialog = false
            }
        )
    }

    expenseToEdit?.let { expense ->
        AddExpenseDialog(
            expense = expense,
            categories = uiState.categories,
            onDismiss = { expenseToEdit = null },
            onSave = { updatedExpense ->
                viewModel.updateExpense(updatedExpense)
                expenseToEdit = null
            }
        )
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete '${expense.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategorySection(
    categoryName: String,
    categoryColor: String,
    expenses: List<Expense>,
    total: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Category header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(categoryColor))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "${expenses.size} expense${if (expenses.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = total.toCurrencyString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextSecondary
                    )
                }
            }
            
            // Expense list
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    expenses.forEach { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = { onEdit(expense) },
                            onDelete = { onDelete(expense) }
                        )
                    }
                }
            }
        }
    }
}
