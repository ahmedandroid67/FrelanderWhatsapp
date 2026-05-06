package com.ahmed.clientflow.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahmed.clientflow.data.AppLanguage
import com.ahmed.clientflow.ui.theme.AppTokens

fun pipelineStepLabel(statusName: String, language: AppLanguage): String = when (statusName) {
    "Lead" -> when (language) {
        AppLanguage.English -> "Lead"
        AppLanguage.French -> "Prospect"
        AppLanguage.Arabic -> "عميل محتمل"
    }
    "Quoted" -> when (language) {
        AppLanguage.English -> "Quoted"
        AppLanguage.French -> "Devis"
        AppLanguage.Arabic -> "عرض سعر"
    }
    "Booked" -> when (language) {
        AppLanguage.English -> "Booked"
        AppLanguage.French -> "Reserve"
        AppLanguage.Arabic -> "محجوز"
    }
    "Completed" -> when (language) {
        AppLanguage.English -> "Completed"
        AppLanguage.French -> "Termine"
        AppLanguage.Arabic -> "مكتمل"
    }
    "Paid" -> when (language) {
        AppLanguage.English -> "Paid"
        AppLanguage.French -> "Paye"
        AppLanguage.Arabic -> "مدفوع"
    }
    else -> statusName
}

data class PipelineStep(
    val label: String,
    val color: Color,
    val bgColor: Color
)

val pipelineSteps = listOf(
    PipelineStep("Lead", AppTokens.Pipeline.lead, AppTokens.Pipeline.leadBg),
    PipelineStep("Quoted", AppTokens.Pipeline.quoted, AppTokens.Pipeline.quotedBg),
    PipelineStep("Booked", AppTokens.Pipeline.booked, AppTokens.Pipeline.bookedBg),
    PipelineStep("Completed", AppTokens.Pipeline.completed, AppTokens.Pipeline.completedBg),
    PipelineStep("Paid", AppTokens.Pipeline.paidUp, AppTokens.Pipeline.paidUpBg)
)

@Composable
fun PipelineRow(
    current: String,
    onSelect: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        pipelineSteps.forEachIndexed { index, step ->
            val isActive = step.label == current
            val isPast = pipelineSteps.indexOfFirst { it.label == current }.let { idx ->
                index < idx || isActive
            }

            val statusColor by animateColorAsState(
                targetValue = if (isPast || isActive) step.color else MaterialTheme.colorScheme.outline,
                animationSpec = spring(),
                label = "pipelineColor"
            )
            val bgColor by animateColorAsState(
                targetValue = if (isActive) step.bgColor else Color.Transparent,
                animationSpec = spring(),
                label = "pipelineBg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .then(
                        Modifier.clip(RoundedCornerShape(10.dp))
                            .then(
                                if (isActive) Modifier.background(
                                    statusColor.copy(alpha = 0.10f),
                                    RoundedCornerShape(10.dp)
                                ) else Modifier
                            )
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = pipelineStepLabel(step.label, language),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) step.color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PipelineStepIndicator(
    current: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pipelineSteps.forEachIndexed { index, step ->
            val isActive = step.label == current
            val isPast = pipelineSteps.indexOfFirst { it.label == current }.let { idx -> index < idx }

            Box(
                modifier = Modifier
                    .size(if (isActive) 28.dp else 20.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> step.color
                            isPast -> AppTokens.Semantic.success
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPast) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            if (index < pipelineSteps.lastIndex) {
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .background(
                            if (isPast) AppTokens.Semantic.success
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(1.dp)
                        )
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
