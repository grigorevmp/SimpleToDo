package com.grigorevmp.simpletodo.ui.notes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val annotated = buildMarkdownAnnotatedString(markdown, colors.onSurfaceVariant)
    Text(text = annotated, style = typography.bodyMedium, modifier = modifier)
}

private fun buildMarkdownAnnotatedString(markdown: String, mutedColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val boldStyle = SpanStyle(fontWeight = FontWeight.SemiBold)
        val italicStyle = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        val codeStyle = SpanStyle(fontFamily = FontFamily.Monospace, background = mutedColor.copy(alpha = 0.12f))
        val h1Style = SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
        val h2Style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        val h3Style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        val quoteStyle = SpanStyle(color = mutedColor)

        var inCodeBlock = false
        val lines = markdown.replace("\r\n", "\n").split("\n")
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine
            if (line.trim().startsWith("```") ) {
                inCodeBlock = !inCodeBlock
                if (index != lines.lastIndex) append("\n")
                return@forEachIndexed
            }

            if (inCodeBlock) {
                pushStyle(codeStyle)
                append(line)
                pop()
                if (index != lines.lastIndex) append("\n")
                return@forEachIndexed
            }

            when {
                line.startsWith("### ") -> {
                    pushStyle(h3Style)
                    appendInlineMarkdown(line.removePrefix("### "), boldStyle, italicStyle, codeStyle)
                    pop()
                }
                line.startsWith("## ") -> {
                    pushStyle(h2Style)
                    appendInlineMarkdown(line.removePrefix("## "), boldStyle, italicStyle, codeStyle)
                    pop()
                }
                line.startsWith("# ") -> {
                    pushStyle(h1Style)
                    appendInlineMarkdown(line.removePrefix("# "), boldStyle, italicStyle, codeStyle)
                    pop()
                }
                line.startsWith("> ") -> {
                    pushStyle(quoteStyle)
                    append("| ")
                    appendInlineMarkdown(line.removePrefix("> "), boldStyle, italicStyle, codeStyle)
                    pop()
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    append("- ")
                    appendInlineMarkdown(line.drop(2), boldStyle, italicStyle, codeStyle)
                }
                else -> {
                    appendInlineMarkdown(line, boldStyle, italicStyle, codeStyle)
                }
            }

            if (index != lines.lastIndex) append("\n")
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(
    text: String,
    boldStyle: SpanStyle,
    italicStyle: SpanStyle,
    codeStyle: SpanStyle
) {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > i + 2) {
                    pushStyle(boldStyle)
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append("**")
                    i += 2
                }
            }
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end > i + 1) {
                    pushStyle(italicStyle)
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append('*')
                    i += 1
                }
            }
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    pushStyle(codeStyle)
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append('`')
                    i += 1
                }
            }
            else -> {
                append(text[i])
                i += 1
            }
        }
    }
}
