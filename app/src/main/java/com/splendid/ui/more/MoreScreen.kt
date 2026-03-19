package com.splendid.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.splendid.ui.Screen
import com.splendid.ui.components.ElevatedCard
import com.splendid.ui.components.GradientBackground
import com.splendid.ui.theme.TextOnGradient
import com.splendid.ui.theme.TextPrimary
import com.splendid.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    GradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "More",
                            color = TextOnGradient
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MoreMenuItem(
                        icon = Icons.Default.Category,
                        title = "Categories",
                        subtitle = "Manage expense categories",
                        onClick = { navController.navigate(Screen.Categories.route) }
                    )
                }
                
                item {
                    MoreMenuItem(
                        icon = Icons.Default.AccountBalance,
                        title = "Budget",
                        subtitle = "Set monthly spending limits",
                        onClick = { navController.navigate(Screen.Budget.route) }
                    )
                }
                
                item {
                    MoreMenuItem(
                        icon = Icons.Default.People,
                        title = "IOU Tracker",
                        subtitle = "Track money owed and receivable",
                        onClick = { navController.navigate(Screen.IOU.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = TextSecondary
                )
            }
        }
    }
}
