package com.ahmed.clientflow.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    animatedValue: Int? = null,
    animatedPrefix: String = ""
) {
    val containerColor by animateColorAsState(
        targetValue = accentColor.copy(alpha = 0.08f),
        animationSpec = spring(),
        label = "statBg"
    )
    val animatedCount by animateIntAsState(
        targetValue = animatedValue ?: 0,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "statCount"
    )

    val displayValue = if (animatedValue != null) "$animatedPrefix$animatedCount" else value

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsRow(
    items: List<StatCardData>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        items.chunked(2).forEach { chunk ->
            Row(modifier = Modifier.fillMaxWidth()) {
                chunk.forEach { data ->
                    StatCard(
                        value = data.value,
                        label = data.label,
                        icon = data.icon,
                        accentColor = data.accentColor,
                        animatedValue = data.animatedValue,
                        animatedPrefix = data.animatedPrefix,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (chunk.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

data class StatCardData(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val animatedValue: Int? = null,
    val animatedPrefix: String = ""
)
