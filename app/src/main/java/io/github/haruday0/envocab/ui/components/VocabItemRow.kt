package io.github.haruday0.envocab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
 */
@Composable
fun VocabItemRow(
    item: VocabItem,
    index: Int,
    count: Int,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.segmentedColors(
            containerColor = cardContainerColor(),
        ),
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (item.isMastered) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = "習得済み",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Spacer(Modifier.width(18.dp))
                }
                Text(
                    text = "${item.no}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 24.dp),
                )
            }
        },
        trailingContent = null,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FormattedMeaningText(
                meaning = item.meaning,
                isMasked = false,
                style = MaterialTheme.typography.bodyMedium,
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
    onDelete: (VocabItem) -> Unit,
    onToggleMastered: (VocabItem, Boolean) -> Unit = { _, _ -> },
    onPlayAudio: (VocabItem, String) -> Unit = { _, _ -> },
) {
    if (item == null) return

    var showMenu by remember(item.id) { mutableStateOf(value = false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(painter = painterResource(R.drawable.ic_more_vert), contentDescription = "メニュー")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("編集") },
                            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit(item)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("削除") },
                            leadingIcon = { Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDelete(item)
                            },
                        )
                    }
                }
            }

            Text(
                text = "No. ${item.no}  (Part ${item.part}${if (item.group.isNotBlank()) " / ${item.group}" else ""})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.word,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            // 音声再生（単語 / 意味）と習得状態トグルボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = { onPlayAudio(item, "en") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("単語")
                }

                FilledTonalButton(
                    onClick = { onPlayAudio(item, "ja") },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("意味")
                }

                if (item.isMastered) {
                    FilledTonalButton(
                        onClick = { onToggleMastered(item, false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_circle),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text("覚えた")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onToggleMastered(item, true) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text("覚えた")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FormattedMeaningText(
                meaning = item.meaning,
                isMasked = false,
                style = MaterialTheme.typography.titleMedium,
            )

            if (item.translation.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = item.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (item.note.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onConfirm: (VocabItem) -> Unit,
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
        },
    )
}
