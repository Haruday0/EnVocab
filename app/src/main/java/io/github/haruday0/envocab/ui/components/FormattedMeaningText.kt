package io.github.haruday0.envocab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 品詞バッジ [名] [動] や重要赤文字 {word} を解析して装飾表示するテキストコンポーネント
 */
@Composable
fun FormattedMeaningText(
    meaning: String,
    isMasked: Boolean,
    style: TextStyle = LocalTextStyle.current
) {
    val circles = listOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳")
    val meaningParts = meaning.split(Regex("""[/／]""")).map { it.trim() }.filter { it.isNotEmpty() }
    val hasMultiple = meaningParts.size > 1

    val baseFontSize = style.fontSize
    val badgeSize = if (baseFontSize == TextUnit.Unspecified) 16.sp else baseFontSize

    val inlineContent = mapOf(
        "posBadge" to InlineTextContent(
            Placeholder(
                width = (badgeSize.value * 1.35).sp,
                height = (badgeSize.value * 1.35).sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) { text ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.extraSmall),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = (badgeSize.value * 0.75).sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = (badgeSize.value * 0.75).sp
                )
            }
        }
    )

    val annotated = buildAnnotatedString {
        meaningParts.forEachIndexed { partIndex, rawPart ->
            if (partIndex > 0) {
                append("  ")
            }

            if (hasMultiple) {
                val circleNumber = circles.getOrElse(partIndex) { "(${partIndex + 1})" }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$circleNumber ")
                }
            }

            var cursor = 0
            val regex = Regex("""\[(.*?)]|\{(.*?)\}""")
            regex.findAll(rawPart).forEach { match ->
                if (match.range.first > cursor) {
                    append(rawPart.substring(cursor, match.range.first))
                }

                val pos = match.groups[1]?.value
                val redText = match.groups[2]?.value

                if (pos != null) {
                    appendInlineContent("posBadge", pos)
                } else if (redText != null) {
                    if (isMasked) {
                        withStyle(
                            SpanStyle(
                                background = Color.Red,
                                color = Color.Transparent
                            )
                        ) {
                            append(" $redText ")
                        }
                    } else {
                        withStyle(
                            SpanStyle(
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(redText)
                        }
                    }
                }
                cursor = match.range.last + 1
            }
            if (cursor < rawPart.length) {
                append(rawPart.substring(cursor))
            }
        }
    }

    Text(
        text = annotated,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        inlineContent = inlineContent
    )
}
