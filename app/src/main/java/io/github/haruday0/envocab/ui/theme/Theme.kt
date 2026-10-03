package io.github.haruday0.envocab.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

private val DarkColorScheme = darkColorScheme(
    primary = PixelBlue80,
    onPrimary = Color(0xFF002F65),
    primaryContainer = Color(0xFF274677),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = PixelBlueGrey80,
    onSecondary = Color(0xFF2B313F),
    secondaryContainer = Color(0xFF414756),
    onSecondaryContainer = Color(0xFFDDE2F2),
    tertiary = PixelTeal80,
    onTertiary = Color(0xFF003731),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    outline = Color(0xFF8E9099)
)

private val LightColorScheme = lightColorScheme(
    primary = PixelBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001944),
    secondary = PixelBlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE2F2),
    onSecondaryContainer = Color(0xFF161C28),
    tertiary = PixelTeal40,
    onTertiary = Color.White,
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    surfaceContainer = Color(0xFFF0F0F7),
    surfaceContainerHigh = Color(0xFFEAEAF1),
    outline = Color(0xFF74777F)
)

@Composable
fun EnVocabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current

    // プレビュー環境ではレイアウトレンダラーのダイナミックカラー不具合を防ぐため、カスタム定義のスキームを使用する
    val colorScheme = when {
        dynamicColor && !isInspection && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
