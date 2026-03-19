package com.splendid.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.splendid.ui.theme.ErrorRed
import com.splendid.ui.theme.TextOnGradient
import com.splendid.utils.CurrencyUtils.toCurrencyString

@Composable
fun BudgetWarningBanner(
    spent: Double,
    limit: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ErrorRed.copy(alpha = 0.2f),
                spotColor = ErrorRed.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRed
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = TextOnGradient,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Budget Exceeded",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextOnGradient
                )
                Text(
                    text = "${spent.toCurrencyString()} of ${limit.toCurrencyString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnGradient.copy(alpha = 0.9f)
                )
            }
        }
    }
}
