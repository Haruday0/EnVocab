package io.github.haruday0.envocab.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import io.github.haruday0.envocab.R

@OptIn(ExperimentalTextApi::class)
val GoogleSans = FontFamily(
    listOf(
        FontWeight.Thin,       // 100
        FontWeight.ExtraLight, // 200
        FontWeight.Light,      // 300
        FontWeight.Normal,     // 400
        FontWeight.Medium,     // 500
        FontWeight.SemiBold,   // 600
        FontWeight.Bold,       // 700
        FontWeight.ExtraBold,  // 800
        FontWeight.Black,      // 900
    ).map { weight ->
        Font(
            resId = R.font.google_sans,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight)
            )
        )
    }
)

/**
 * Google BudouX / Android 14+ 準拠の日本語文節・単語単位での折り返し設定。
 * 単語や文節の途中で不自然に改行されるのを防止します。
 */
val JapaneseLineBreak = LineBreak(
    strategy = LineBreak.Strategy.HighQuality,
    strictness = LineBreak.Strictness.Strict,
    wordBreak = LineBreak.WordBreak.Phrase
)

private fun TextStyle.withAppDefaults(): TextStyle =
    copy(
        fontFamily = GoogleSans,
        lineBreak = JapaneseLineBreak,
    )

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.withAppDefaults(),
    displayMedium = defaultTypography.displayMedium.withAppDefaults(),
    displaySmall = defaultTypography.displaySmall.withAppDefaults(),
    headlineLarge = defaultTypography.headlineLarge.withAppDefaults(),
    headlineMedium = defaultTypography.headlineMedium.withAppDefaults(),
    headlineSmall = defaultTypography.headlineSmall.withAppDefaults(),
    titleLarge = defaultTypography.titleLarge.withAppDefaults(),
    titleMedium = defaultTypography.titleMedium.withAppDefaults(),
    titleSmall = defaultTypography.titleSmall.withAppDefaults(),
    bodyLarge = defaultTypography.bodyLarge.withAppDefaults(),
    bodyMedium = defaultTypography.bodyMedium.withAppDefaults(),
    bodySmall = defaultTypography.bodySmall.withAppDefaults(),
    labelLarge = defaultTypography.labelLarge.withAppDefaults(),
    labelMedium = defaultTypography.labelMedium.withAppDefaults(),
    labelSmall = defaultTypography.labelSmall.withAppDefaults(),
)
