package com.splendid.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.splendid.ui.components.ElevatedCard
import com.splendid.ui.components.GradientBackground
import com.splendid.ui.components.GradientCard
import com.splendid.ui.home.HomeViewModel
import com.splendid.ui.theme.TextOnGradient
import com.splendid.ui.theme.TextPrimary
import com.splendid.ui.theme.TextSecondary
import com.splendid.utils.CurrencyUtils.toCurrencyString
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

data class CategoryExpense(
    val categoryName: String,
    val categoryColor: String,
    val total: Double,
    val percentage: Float,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenses = uiState.monthlyExpenses
    val budgetStatus = uiState.budgetStatus
    
    // Calculate category-wise breakdown
    val categoryBreakdown = expenses
        .groupBy { expense -> expense.categoryName }
        .map { entry ->
            val category = entry.key
            val expenseList = entry.value
            CategoryExpense(
                categoryName = category,
                categoryColor = expenseList.firstOrNull()?.categoryColor ?: "#546E7A",
                total = expenseList.sumOf { expense -> expense.total },
                percentage = 0f,
                count = expenseList.size
            )
        }
        .sortedByDescending { categoryExpense -> categoryExpense.total }
    
    val totalExpense = categoryBreakdown.sumOf { categoryExpense -> categoryExpense.total }
    val categoryBreakdownWithPercentage = categoryBreakdown.map { categoryExpense ->
        categoryExpense.copy(percentage = if (totalExpense > 0) (categoryExpense.total / totalExpense * 100).toFloat() else 0f)
    }

    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Analytics",
                                color = TextOnGradient,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                                color = TextOnGradient.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Budget vs Spending Card
                budgetStatus?.let { status ->
                    item {
                        BudgetVsSpendingCard(
                            budget = status.budget?.limitAmount ?: 0.0,
                            spent = status.currentSpending,
                            remaining = status.remainingAmount,
                            isOverBudget = status.isOverBudget
                        )
                    }
                }

                // Total expense card
                item {
                    GradientCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Expenses",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnGradient
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = totalExpense.toCurrencyString(),
                                style = MaterialTheme.typography.displaySmall,
                                color = TextOnGradient
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${expenses.size} transaction${if (expenses.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOnGradient.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Category breakdown with pie chart
                if (categoryBreakdownWithPercentage.isEmpty()) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No expenses yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start adding expenses to see analytics",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Expense Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextOnGradient
                        )
                    }
                    
                    item {
                        CategoryPieChart(categories = categoryBreakdownWithPercentage)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPieChart(categories: List<CategoryExpense>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pie chart
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            ) {
                val canvasSize = size.minDimension
                val radius = canvasSize / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                
                var startAngle = -90f
                
                categories.forEach { category ->
                    val sweepAngle = (category.percentage / 100f) * 360f
                    val color = try {
                        Color(android.graphics.Color.parseColor(category.categoryColor))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                    
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    startAngle += sweepAngle
                }
                
                // Draw white circle in center for donut effect
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.5f,
                    center = center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Legend
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                            Color(android.graphics.Color.parseColor(category.categoryColor))
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = category.total.toCurrencyString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format("%.1f", category.percentage)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetVsSpendingCard(
    budget: Double,
    spent: Double,
    remaining: Double,
    isOverBudget: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Budget vs Spending",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Budget and Spent amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = budget.toCurrencyString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = spent.toCurrencyString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Remaining amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOverBudget) "Over Budget" else "Remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = if (isOverBudget) 
                        "- ${(-remaining).toCurrencyString()}" 
                    else 
                        remaining.toCurrencyString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            // Percentage
            if (budget > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format("%.1f", (spent / budget * 100))}% of budget used",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
