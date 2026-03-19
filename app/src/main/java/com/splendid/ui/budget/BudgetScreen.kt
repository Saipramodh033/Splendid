package com.splendid.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.splendid.ui.components.ElevatedCard
import com.splendid.ui.components.GradientBackground
import com.splendid.ui.components.GradientButton
import com.splendid.ui.components.GradientCard
import com.splendid.ui.theme.TextOnGradient
import com.splendid.ui.theme.TextPrimary
import com.splendid.ui.theme.TextSecondary
import com.splendid.utils.CurrencyUtils.toCurrencyString
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var budgetAmount by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Budget",
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
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        ) { padding ->
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Month/Year display with gradient
                item {
                    GradientCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${getMonthName(getCurrentMonth())} ${getCurrentYear()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextOnGradient
                        )
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnGradient.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // Current budget display
                if (uiState.currentBudget != null) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Limit",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = uiState.currentBudget!!.limitAmount.toCurrencyString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Budget input
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Set Monthly Limit",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = budgetAmount,
                            onValueChange = { budgetAmount = it },
                            label = { Text("Amount") },
                            placeholder = { Text("Enter amount") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Text(
                                    text = "₹",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You'll receive a warning if expenses exceed this limit",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                // Save button
                item {
                    Button(
                        onClick = {
                            val amount = budgetAmount.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                viewModel.saveBudget(amount)
                                showSuccessMessage = true
                                budgetAmount = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = budgetAmount.toDoubleOrNull()?.let { it > 0 } == true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.splendid.ui.theme.ButtonOnGradient,
                            contentColor = com.splendid.ui.theme.ButtonTextOnGradient
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Budget")
                    }
                }
                
                // Success message
                if (showSuccessMessage) {
                    item {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            showSuccessMessage = false
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                text = "Budget saved successfully",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
private fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
    }
}
