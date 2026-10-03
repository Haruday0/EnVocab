package io.github.haruday0.envocab.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * ライトテーマとダークテーマを同時にプレビューするためのマルチプレビューアノテーション。
 */
@Preview(
    name = "Light Mode",
    group = "Theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    locale = "ja"
)
@Preview(
    name = "Dark Mode",
    group = "Theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ja"
)
annotation class ThemePreviews
