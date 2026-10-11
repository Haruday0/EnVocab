package io.github.haruday0.envocab.ui.components

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn

/**
 * Material Design 3 / MDC 公式仕様の Fade through (`MaterialFadeThrough`)。
 * 空間的な関係がない画面同士の切り替え（ボトムナビゲーションなど）に使う。
 * 出る画面が素早くフェードアウトし、入る画面が拡大しながらフェードインします。
 */
fun materialFadeThrough(): ContentTransform {
    val duration = 200
    val easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val enter = fadeIn(
        animationSpec = tween(
            durationMillis = 140,
            delayMillis = 60,
            easing = easing,
        ),
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = duration,
            easing = easing,
        ),
        initialScale = 0.95f,
    )
    val exit = fadeOut(
        animationSpec = tween(
            durationMillis = 60,
            easing = easing,
        ),
    )
    return ContentTransform(enter, exit, sizeTransform = SizeTransform(clip = false))
}
