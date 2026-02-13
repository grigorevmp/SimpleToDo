package com.grigorevmp.simpletodo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun CircleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTapOffset: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val border = if (checked) primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    val fill = if (checked) primary else Color.Transparent

    Surface(
        shape = CircleShape,
        color = fill,
        border = BorderStroke(2.dp, border),
        modifier = modifier
            .size(22.dp)
            .semantics { role = Role.Checkbox }
            .pointerInput(enabled, checked) {
                detectTapGestures { offset ->
                    if (!enabled) return@detectTapGestures
                    onTapOffset?.invoke(offset)
                    onCheckedChange(!checked)
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (checked) {
                Icon(
                    imageVector = SimpleIcons.Check,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
