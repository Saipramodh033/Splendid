package com.splendid.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.splendid.domain.model.Expense
import com.splendid.ui.components.AddExpenseDialog
import com.splendid.ui.components.ExpenseCard
import com.splendid.ui.components.GradientBackground
import com.splendid.ui.components.GradientCard
import com.splendid.ui.theme.TextOnGradient
import com.splendid.ui.theme.TextSecondary
import com.splendid.utils.CurrencyUtils.toCurrencyString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    navController: NavController,
    dateMillis: Long,
    viewModel: CalendarViewModel = viewModel()
) {
    val date = remember(dateMillis) {
        Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    
    // Select the date when screen opens
    LaunchedEffect(date) {
        viewModel.selectDate(date)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                            color = TextOnGradient
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextOnGradient
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
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add expense")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Daily total card
                item {
                    GradientCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnGradient
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.selectedDateTotal.toCurrencyString(),
                                style = MaterialTheme.typography.displaySmall,
                                color = TextOnGradient,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${uiState.selectedDateExpenses.size} expense${if (uiState.selectedDateExpenses.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOnGradient.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                // Expense list
                if (uiState.selectedDateExpenses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No expenses yet",
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
                    items(uiState.selectedDateExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = { expenseToEdit = expense },
                            onDelete = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }
    }
    
    // Add expense dialog
    if (showAddDialog) {
        AddExpenseDialog(
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onSave = { expense ->
                // Set the expense date to the selected date
                val expenseWithDate = expense.copy(
                    date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                viewModel.addExpense(expenseWithDate)
                showAddDialog = false
            }
        )
    }
    
    // Edit expense dialog
    if (expenseToEdit != null) {
        AddExpenseDialog(
            expense = expenseToEdit,
            categories = uiState.categories,
            onDismiss = { expenseToEdit = null },
            onSave = { expense ->
                viewModel.updateExpense(expense)
                expenseToEdit = null
            }
        )
    }
    
    // Delete confirmation dialog
    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete \"${expenseToDelete?.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { expense ->
                            viewModel.deleteExpense(expense)
                            expenseToDelete = null
                        }
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
