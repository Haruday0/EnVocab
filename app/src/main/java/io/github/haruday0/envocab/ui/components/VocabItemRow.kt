package io.github.haruday0.envocab.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.model.VocabItem

/**
 * 単語リストの 1 行を表示するコンポーネント
 * 再生状態に応じて円形から角丸四角形へとモーフィングする音声ボタンを備えます。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VocabItemRow(
    item: VocabItem,
    index: Int,
    count: Int,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onClick: () -> Unit
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.segmentedColors(
            containerColor = cardContainerColor()
        ),
        leadingContent = {
            val cornerRadius by animateDpAsState(
                targetValue = if (isPlaying) 12.dp else 24.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "audioShape"
            )
            val containerColor by animateColorAsState(
                targetValue = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "audioContainer"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "audioContent"
            )

            FilledIconButton(
                onClick = onPlayAudio,
                shape = RoundedCornerShape(cornerRadius),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                    },
                    label = "audioIcon"
                ) { playing ->
                    Icon(
                        painter = painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription = if (playing) "音声を停止" else "音声を再生"
                    )
                }
            }
        },
        supportingContent = {
            FormattedMeaningText(
                meaning = item.meaning,
                isMasked = false,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${item.no}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 単語の詳細表示および編集・削除操作ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabItemDetailSheet(
    item: VocabItem?,
    onDismiss: () -> Unit,
    onEdit: (VocabItem) -> Unit,
    onDelete: (VocabItem) -> Unit
) {
    if (item == null) return

    var showMenu by remember(item.id) { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(painter = painterResource(R.drawable.ic_more_vert), contentDescription = "メニュー")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("編集") },
                            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit(item)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("削除") },
                            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDelete(item)
                            }
                        )
                    }
                }
            }

            Text(
                text = "No. ${item.no}  (Part ${item.part}${if (item.group.isNotBlank()) " / ${item.group}" else ""})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.word,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            FormattedMeaningText(
                meaning = item.meaning,
                isMasked = false,
                style = MaterialTheme.typography.titleMedium
            )

            if (item.translation.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = item.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.note.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 単語削除確認ダイアログ
 */
@Composable
fun DeleteVocabItemDialog(
    item: VocabItem?,
    onDismiss: () -> Unit,
    onConfirm: (VocabItem) -> Unit
) {
    if (item == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("単語を削除") },
        text = { Text("「${item.word}」を削除しますか？この操作は取り消せません。") },
        confirmButton = {
            TextButton(onClick = { onConfirm(item) }) {
                Text("削除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
