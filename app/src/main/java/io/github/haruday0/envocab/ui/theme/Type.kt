package io.github.haruday0.envocab.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak

/**
 * Google BudouX / Android 14+ 準拠の日本語文節・単語単位での折り返し設定。
 * 単語や文節の途中で不自然に改行されるのを防止します。
 */
val JapaneseLineBreak = LineBreak(
    strategy = LineBreak.Strategy.HighQuality,
    strictness = LineBreak.Strictness.Strict,
    wordBreak = LineBreak.WordBreak.Phrase
)

private fun TextStyle.withJapaneseLineBreak(): TextStyle =
    copy(lineBreak = JapaneseLineBreak)

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.withJapaneseLineBreak(),
    displayMedium = defaultTypography.displayMedium.withJapaneseLineBreak(),
    displaySmall = defaultTypography.displaySmall.withJapaneseLineBreak(),
    headlineLarge = defaultTypography.headlineLarge.withJapaneseLineBreak(),
    headlineMedium = defaultTypography.headlineMedium.withJapaneseLineBreak(),
    headlineSmall = defaultTypography.headlineSmall.withJapaneseLineBreak(),
    titleLarge = defaultTypography.titleLarge.withJapaneseLineBreak(),
    titleMedium = defaultTypography.titleMedium.withJapaneseLineBreak(),
    titleSmall = defaultTypography.titleSmall.withJapaneseLineBreak(),
    bodyLarge = defaultTypography.bodyLarge.withJapaneseLineBreak(),
    bodyMedium = defaultTypography.bodyMedium.withJapaneseLineBreak(),
    bodySmall = defaultTypography.bodySmall.withJapaneseLineBreak(),
    labelLarge = defaultTypography.labelLarge.withJapaneseLineBreak(),
    labelMedium = defaultTypography.labelMedium.withJapaneseLineBreak(),
    labelSmall = defaultTypography.labelSmall.withJapaneseLineBreak(),
)
