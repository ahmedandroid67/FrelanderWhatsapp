package com.ahmed.clientflow.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

fun Modifier.animatePress(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "pressScale"
    )
    this
        .scale(scale)
        .then(Modifier)
}

@Composable
fun AnimatedListItem(
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = spring(dampingRatio = 0.6f)) +
                scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = 0.6f))
    ) {
        content()
    }
}
