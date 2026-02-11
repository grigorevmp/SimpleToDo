package com.grigorevmp.simpletodo.ui.notes.create

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp


@Composable
fun MarkdownToolbar(
    onWrapBold: () -> Unit,
    onWrapItalic: () -> Unit,
    onWrapCode: () -> Unit,
    onH1: () -> Unit,
    onH2: () -> Unit,
    onBullet: () -> Unit,
    onTodo: () -> Unit,
    onOrdered: () -> Unit,
    onQuote: () -> Unit,
    onCodeBlock: () -> Unit,
    onLink: () -> Unit
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
    ) {
        TextButton(onClick = onWrapBold) {
            Text(
                text = "B",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onWrapItalic) {
            Text(
                text = "I",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onWrapCode) {
            Text(
                text = "`",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onCodeBlock) {
            Text(
                text = "```",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onLink) {
            Text(
                text = "Link",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onH1) {
            Text(
                text = "H1",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onH2) {
            Text(
                text = "H2",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onBullet) {
            Text(
                text = "-",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onTodo) {
            Text(
                text = "[ ]",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onOrdered) {
            Text(
                text = "1.",
                fontWeight = Bold
            )
        }
        TextButton(onClick = onQuote) {
            Text(
                text = ">",
                fontWeight = Bold
            )
        }
    }
}