package io.github.haruday0.envocab.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Android標準の画面遷移アニメーションと、左右端スワイプによる予測型「戻る」ジェスチャー
 * (Predictive Back Gesture) に対応したコンテナ。
 */
@Composable
fun PredictiveBackContainer(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    content: @Composable (requestBack: () -> Unit) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidthPx = windowInfo.containerSize.width.toFloat()
    val maxDragOffsetPx = remember(density) {
        with(density) { 56.dp.toPx() }
    }

    val offsetX = remember { Animatable(screenWidthPx) }
    val gestureProgress = remember { Animatable(0f) }
    val gestureEdge = remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    val isGesturing = remember { mutableStateOf(false) }
    val isExiting = remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        // 画面入場アニメーション (右からスライドイン)
        LaunchedEffect(Unit) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        }

        // プログラムによる戻る処理 (AppBarの戻るボタン等)
        fun requestBack() {
            if (isExiting.value) return
            isExiting.value = true
            keyboardController?.hide()
            coroutineScope.launch {
                offsetX.animateTo(
                    targetValue = screenWidthPx,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
                onBack()
            }
        }

        // Android 14+ 予測型「戻る」ジェスチャーハンドラ
        PredictiveBackHandler(enabled = !isExiting.value) { progressFlow ->
            isGesturing.value = true
            keyboardController?.hide()
            try {
                progressFlow.collect { backEvent ->
                    gestureEdge.intValue = backEvent.swipeEdge
                    gestureProgress.snapTo(backEvent.progress)
                }
                // ジェスチャー完了 (コミット)
                isExiting.value = true
                withContext(NonCancellable) {
                    val targetX = if (gestureEdge.intValue == BackEventCompat.EDGE_LEFT) {
                        screenWidthPx
                    } else {
                        -screenWidthPx
                    }
                    offsetX.animateTo(
                        targetValue = targetX,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    )
                    onBack()
                }
            } catch (e: CancellationException) {
                // ジェスチャーキャンセル (元の全画面状態へバネアニメーションで復元)
                withContext(NonCancellable) {
                    gestureProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    isGesturing.value = false
                }
                throw e
            }
        }

        val progress = gestureProgress.value
        val scale = 1f - (progress * 0.10f)
        val cornerRadius = (progress * 28).dp
        val dragShift = if (gestureEdge.intValue == BackEventCompat.EDGE_LEFT) {
            progress * maxDragOffsetPx
        } else {
            -progress * maxDragOffsetPx
        }
        val totalTranslationX = offsetX.value + dragShift

        // 背景との境界を自然にする scrim
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (1f - progress) * 0.12f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = totalTranslationX
                    scaleX = scale
                    scaleY = scale
                    clip = cornerRadius > 0.dp
                    shape = RoundedCornerShape(cornerRadius)
                }
                .shadow(
                    elevation = (progress * 10).dp,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            content(::requestBack)
        }
    }
}
