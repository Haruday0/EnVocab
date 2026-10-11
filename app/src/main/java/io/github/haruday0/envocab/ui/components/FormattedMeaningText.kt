package io.github.haruday0.envocab.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

private val MeaningSplitRegex = Regex("""[/／]""")
private val TagRegex = Regex("""\[(.*?)]|\{(.*?)\}""")
private val Circles = listOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱", "⑲", "⑳")

/**
 * 品詞バッジ　[名]　[動]　や重要赤文字 {word} を解析して装飾表示するテキストコンポーネント。
 * ダークテーマでも高いコントラストを保ち視認性を確保します。
 */
@Composable
fun FormattedMeaningText(
    meaning: String,
    isMasked: Boolean,
    style: TextStyle = LocalTextStyle.current,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val isDark = isSystemInDarkTheme()
    val baseFontSize = style.fontSize
    val badgeSize = if (baseFontSize == TextUnit.Unspecified) 16.sp else baseFontSize

    // 参考書・赤シート定番の朱色系（ライト: #E53935, ダーク: #FF5252）
    val vermilionRed = if (isDark) Color(0xFFFF5252) else Color(0xFFE53935)

    val inlineContent = remember(badgeSize, isDark) {
        mapOf(
            "posBadge" to InlineTextContent(
                Placeholder(
                    width = (badgeSize.value * 1.35).sp,
                    height = (badgeSize.value * 1.35).sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                )
            ) { text ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.extraSmall
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = (badgeSize.value * 0.75).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (badgeSize.value * 0.75).sp,
                    )
                }
            }
        )
    }

    val annotated = remember(meaning, isMasked, vermilionRed, vermilionRed) {
        val meaningParts = meaning.split(MeaningSplitRegex).asSequence()
            .map { it.trim() }.filter { it.isNotEmpty() }.toList()
        val hasMultiple = meaningParts.size > 1

        buildAnnotatedString {
            meaningParts.forEachIndexed { partIndex, rawPart ->
                if (partIndex > 0) {
                    append("  ")
                }

                if (hasMultiple) {
                    val circleNumber = Circles.getOrElse(partIndex) { "(${partIndex + 1})" }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$circleNumber ")
                    }
                }

                var cursor = 0
                TagRegex.findAll(rawPart).forEach { match ->
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
                                    background = vermilionRed,
                                    color = Color.Transparent,
                                    fontWeight = FontWeight.Bold,
                                )
                            ) {
                                append(redText)
                            }
                        } else {
                            withStyle(
                                SpanStyle(
                                    color = vermilionRed,
                                    fontWeight = FontWeight.Bold,
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
    }

    Text(
        text = annotated,
        style = style,
        color = color,
        inlineContent = inlineContent,
    )
}
