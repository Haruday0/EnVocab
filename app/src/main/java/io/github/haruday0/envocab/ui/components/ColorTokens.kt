package io.github.haruday0.envocab.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * 画面のキャンバス背景色
 * ライトテーマ: surfaceContainer (Tone 94)
 * ダークテーマ: surface (Tone 6)
 */
@Composable
fun screenBackgroundColor(): Color =
    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer

/**
 * カードおよびグルーピングされたリストアイテムの背景色
 * ライトテーマ: surface (Tone 98 / 白)
 * ダークテーマ: surfaceContainer (Tone 12)
 */
@Composable
fun cardContainerColor(): Color =
    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface

/**
 * 検索画面の初期表示バッジに使用するスカラップ形状
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun searchEmptyBadgeShape(): Shape =
    MaterialShapes.Cookie9Sided.toShape()
