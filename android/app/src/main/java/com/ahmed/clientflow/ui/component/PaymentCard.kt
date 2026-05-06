package com.ahmed.clientflow.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahmed.clientflow.ui.theme.AppTokens

@Composable
fun PaymentSummaryCard(
    clientName: String,
    totalAmount: Double,
    paidAmount: Double,
    dueDate: String,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    currencySymbol: String = "$"
) {
    val progress by animateFloatAsState(
        targetValue = if (totalAmount > 0) (paidAmount / totalAmount).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = spring(),
        label = "paymentProgress"
    )

    val progressColor = when {
        progress >= 1f -> AppTokens.Semantic.success
        progress >= 0.5f -> AppTokens.Semantic.warning
        else -> AppTokens.Semantic.error
    }

    val containerColor = if (accent) AppTokens.Surface.overdueCard else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (accent) 3.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(text = status, color = progressColor)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaymentLabel("Paid", "${currencySymbol}${formatAmount(paidAmount)}")
                PaymentLabel("Balance", "${currencySymbol}${formatAmount(totalAmount - paidAmount)}", isBalance = true)
                PaymentLabel("Due", dueDate.ifBlank { "-" })
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun PaymentLabel(label: String, value: String, isBalance: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isBalance && value.startsWith("-")) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun OverduePaymentCard(
    clientName: String,
    balance: Double,
    dueDate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTokens.Surface.overdueCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTokens.Semantic.error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppTokens.Semantic.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clientName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${currencySymbol}${formatAmount(balance)} due by $dueDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTokens.Semantic.error
                )
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) amount.toLong().toString()
    else "%.2f".format(amount)
}
