package com.splendid.ui.iou

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.splendid.domain.model.IOU
import com.splendid.domain.model.IOUType
import com.splendid.ui.components.ElevatedCard
import com.splendid.ui.components.GradientBackground
import com.splendid.ui.components.GradientButton
import com.splendid.ui.components.GradientCard
import com.splendid.ui.theme.*
import com.splendid.utils.CurrencyUtils.toCurrencyString
import com.splendid.utils.DateUtils.toDateString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOUScreen(
    navController: NavController,
    viewModel: IOUViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var iouToSettle by remember { mutableStateOf<IOU?>(null) }
    
    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Borrowed & Lent",
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
            },
            floatingActionButton = {
                GradientButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add IOU")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Net Balance Card with gradient
                GradientCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    gradientColors = if (uiState.netBalance >= 0) 
                        listOf(CardGradientStart, CardGradientEnd)
                    else 
                        listOf(ErrorRed, ErrorRed.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnGradient
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.netBalance.toCurrencyString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextOnGradient
                    )
                    Text(
                        text = when {
                            uiState.netBalance > 0 -> "To Receive"
                            uiState.netBalance < 0 -> "To Pay"
                            else -> "Settled"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnGradient.copy(alpha = 0.9f)
                    )
                }
                
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = TextOnGradient
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Pending") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("History") }
                    )
                }
                
                // Content
                val ious = if (selectedTab == 0) uiState.pendingIOUs else uiState.settledIOUs
                
                if (ious.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0) "No pending IOUs" else "No settled IOUs",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextOnGradient.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ious) { iou ->
                            IOUCard(
                                iou = iou,
                                onSettle = if (selectedTab == 0) {
                                    { iouToSettle = iou }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add IOU Dialog
    if (showAddDialog) {
        AddIOUDialog(
            onDismiss = { showAddDialog = false },
            onSave = { iou ->
                viewModel.addIOU(iou)
                showAddDialog = false
            }
        )
    }
    
    // Settle confirmation
    iouToSettle?.let { iou ->
        AlertDialog(
            onDismissRequest = { iouToSettle = null },
            title = { Text("Settle IOU") },
            text = { Text("Mark this IOU as settled?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.settleIOU(iou)
                    iouToSettle = null
                }) {
                    Text("Settle")
                }
            },
            dismissButton = {
                TextButton(onClick = { iouToSettle = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun IOUCard(
    iou: IOU,
    onSettle: (() -> Unit)?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = iou.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = iou.date.toDateString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = iou.amount.toCurrencyString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (iou.type == IOUType.WILL_RECEIVE) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                if (onSettle != null) {
                    TextButton(onClick = onSettle) {
                        Text("Settle")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIOUDialog(
    onDismiss: () -> Unit,
    onSave: (IOU) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(IOUType.WILL_RECEIVE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add IOU") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("₹") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == IOUType.WILL_RECEIVE,
                        onClick = { selectedType = IOUType.WILL_RECEIVE },
                        label = { Text("Will Receive") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == IOUType.I_OWE,
                        onClick = { selectedType = IOUType.I_OWE },
                        label = { Text("I Owe") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (title.isNotBlank() && amt != null && amt > 0) {
                        onSave(
                            IOU(
                                title = title,
                                amount = amt,
                                type = selectedType,
                                date = System.currentTimeMillis(),
                                isSettled = false
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
