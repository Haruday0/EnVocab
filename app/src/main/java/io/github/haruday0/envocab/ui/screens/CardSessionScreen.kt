package io.github.haruday0.envocab.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import io.github.haruday0.envocab.ui.components.EmptyBookView
import io.github.haruday0.envocab.ui.components.FormattedMeaningText
import io.github.haruday0.envocab.ui.components.VocabItemDetailSheet
import io.github.haruday0.envocab.ui.components.VocabItemRow
import io.github.haruday0.envocab.ui.components.cardContainerColor
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews

/**
 * カード学習の設定画面（ボトムナビゲーションバーのあるメインタブ内に配置）
 */
@Composable
fun CardSetupScreen(
    activeBook: VocabBook?,
    items: List<VocabItem>,
    onStartSession: (List<VocabItem>) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val savedConfig = remember { DataManager.loadCardConfig(context) }

    val availableParts = remember(items) { items.map { it.part }.distinct().sorted() }
    val presetCounts = remember { listOf(10, 20, 30, 50, 100) }

    var isCustomCount by remember { mutableStateOf(savedConfig.isCustomCount) }
    var selectedPreset by remember {
        val count = savedConfig.questionCountText.toIntOrNull() ?: 20
        mutableStateOf(if (savedConfig.isCustomCount) null else if (count in presetCounts) count else 20)
    }
    var customCountText by remember {
        mutableStateOf(if (savedConfig.isCustomCount) savedConfig.questionCountText else "20")
    }

    var selectedParts by remember(availableParts) {
        mutableStateOf(
            if (savedConfig.selectedParts.isNotEmpty()) {
                val valid = savedConfig.selectedParts.intersect(availableParts.toSet())
                valid.ifEmpty { availableParts.toSet() }
            } else {
                availableParts.toSet()
            }
        )
    }
    var onlyUnmastered by remember { mutableStateOf(savedConfig.onlyUnmastered) }
    var isShuffle by remember { mutableStateOf(savedConfig.isShuffle) }

    // 設定変更時の永続化
    LaunchedEffect(selectedParts, onlyUnmastered, isShuffle, isCustomCount, selectedPreset, customCountText) {
        val countStr = if (isCustomCount) customCountText else (selectedPreset?.toString() ?: "20")
        DataManager.saveCardConfig(
            context,
            DataManager.CardConfig(
                selectedParts = selectedParts,
                onlyUnmastered = onlyUnmastered,
                isShuffle = isShuffle,
                questionCountText = countStr,
                isCustomCount = isCustomCount
            )
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = screenBackgroundColor(),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        "カード",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBackgroundColor(),
                    scrolledContainerColor = screenBackgroundColor()
                )
            )
        }
    ) { innerPadding ->
        if (activeBook == null) {
            EmptyBookView(
                modifier = Modifier.padding(innerPadding),
                onNavigateToSettings = onNavigateToSettings
            )
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "単語が登録されていません",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize()
                    .imePadding()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("出題範囲", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableParts.forEach { part ->
                        val isPartSelected = selectedParts.contains(part)
                        FilterChip(
                            selected = isPartSelected,
                            onClick = {
                                selectedParts = if (isPartSelected) {
                                    selectedParts - part
                                } else {
                                    selectedParts + part
                                }
                            },
                            label = { Text("Part $part") }
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedListItem(
                        onClick = { onlyUnmastered = !onlyUnmastered },
                        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = cardContainerColor()
                        ),
                        supportingContent = { Text("覚えた単語を除外します") },
                        trailingContent = {
                            Switch(checked = onlyUnmastered, onCheckedChange = { onlyUnmastered = it })
                        }
                    ) {
                        Text("未習得の単語のみ")
                    }

                    SegmentedListItem(
                        onClick = { isShuffle = !isShuffle },
                        shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = cardContainerColor()
                        ),
                        supportingContent = { Text("ランダムに出題します") },
                        trailingContent = {
                            Switch(checked = isShuffle, onCheckedChange = { isShuffle = it })
                        }
                    ) {
                        Text("ランダム出題")
                    }
                }

                Text("出題数", style = MaterialTheme.typography.titleMedium)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetCounts.forEach { count ->
                        val isSelected = !isCustomCount && selectedPreset == count
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomCount = false
                                selectedPreset = count
                                focusManager.clearFocus()
                            },
                            label = { Text("${count}問") }
                        )
                    }
                    FilterChip(
                        selected = isCustomCount,
                        onClick = {
                            isCustomCount = true
                            selectedPreset = null
                        },
                        label = { Text("カスタム") }
                    )
                }

                if (isCustomCount) {
                    OutlinedTextField(
                        value = customCountText,
                        onValueChange = { value ->
                            if (value.all(Char::isDigit) && value.length <= 4) {
                                customCountText = value
                            }
                        },
                        label = { Text("出題数を入力") },
                        suffix = {
                            Text(
                                "問",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Spacer(Modifier.weight(1f))

                val matchingCount = remember(items, selectedParts, onlyUnmastered) {
                    items.count {
                        (it.part in selectedParts) &&
                                (!onlyUnmastered || !it.isMastered)
                    }
                }

                if (matchingCount == 0 && items.isNotEmpty()) {
                    Text(
                        text = "選択した条件に一致する単語がありません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        var filtered = items.filter {
                            (it.part in selectedParts) &&
                                    (!onlyUnmastered || !it.isMastered)
                        }

                        if (isShuffle) {
                            filtered = filtered.shuffled()
                        }

                        val count = if (isCustomCount) {
                            customCountText.toIntOrNull()?.coerceAtLeast(1) ?: 20
                        } else {
                            selectedPreset ?: 20
                        }
                        filtered = filtered.take(count)

                        if (filtered.isNotEmpty()) {
                            onStartSession(filtered)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = matchingCount > 0
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(
                        "学習を開始",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * カード学習画面 & 完了一覧画面（独立したフルスクリーンとして遷移）
 */
@Composable
fun CardPlayScreen(
    sessionItems: List<VocabItem>,
    onPlayAudio: (Int, String) -> Unit,
    onToggleMastered: (VocabItem, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var currentItems by remember { mutableStateOf(sessionItems) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isMaskRevealed by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var selectedItemForDetail by remember { mutableStateOf<VocabItem?>(null) }

    // 開始時に最初の英単語音声を再生
    LaunchedEffect(Unit) {
        if (currentItems.isNotEmpty()) {
            onPlayAudio(currentItems[0].no, "en")
        }
    }

    if (currentItems.isEmpty() || currentIndex >= currentItems.size) {
        // 学習完了画面：解いた単語が順番にずらっと並ぶ一覧画面
        BackHandler {
            onDismiss()
        }

        Scaffold(
            containerColor = screenBackgroundColor(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = screenBackgroundColor(),
                        scrolledContainerColor = screenBackgroundColor()
                    ),
                    title = {
                        Text("学習完了")
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "閉じる"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                androidx.compose.material3.Surface(
                    color = screenBackgroundColor(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "終了",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor()
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🎉 お疲れさまでした！",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${currentItems.size} 個の単語を完了しました",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            val masteredCount = currentItems.count { it.isMastered }
                            Text(
                                "習得: $masteredCount 個 / 未習得: ${currentItems.size - masteredCount} 個",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "今回の単語 (${currentItems.size}語)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                itemsIndexed(
                    currentItems,
                    key = { _, item -> item.id }
                ) { index, item ->
                    VocabItemRow(
                        item = item,
                        index = index,
                        count = currentItems.size,
                        onClick = {
                            selectedItemForDetail = item
                        }
                    )
                }
            }

            if (selectedItemForDetail != null) {
                VocabItemDetailSheet(
                    item = selectedItemForDetail,
                    onDismiss = { selectedItemForDetail = null },
                    onEdit = {},
                    onDelete = {},
                    onToggleMastered = { item, isMastered ->
                        onToggleMastered(item, isMastered)
                        currentItems = currentItems.map {
                            if (it.id == item.id) it.copy(isMastered = isMastered) else it
                        }
                        selectedItemForDetail = selectedItemForDetail?.copy(isMastered = isMastered)
                    },
                    onPlayAudio = { item, lang ->
                        onPlayAudio(item.no, lang)
                    }
                )
            }
        }
    } else {
        val currentItem = currentItems[currentIndex]

        BackHandler {
            showQuitDialog = true
        }

        if (showQuitDialog) {
            AlertDialog(
                onDismissRequest = { showQuitDialog = false },
                title = {
                    Text("学習を中断しますか？")
                },
                text = {
                    Text("現在の学習の進行状況は破棄されます。")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showQuitDialog = false
                            onDismiss()
                        }
                    ) {
                        Text("中断する")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showQuitDialog = false }
                    ) {
                        Text("続ける")
                    }
                }
            )
        }

        Scaffold(
            containerColor = screenBackgroundColor(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = screenBackgroundColor(),
                        scrolledContainerColor = screenBackgroundColor()
                    ),
                    title = {
                        Text("${currentIndex + 1} / ${currentItems.size}")
                    },
                    navigationIcon = {
                        IconButton(onClick = { showQuitDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "戻る"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "cardFlipTransition"
                ) { targetIndex ->
                    val item = currentItems.getOrNull(targetIndex) ?: return@AnimatedContent
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (!isMaskRevealed) {
                                    isMaskRevealed = true
                                    onPlayAudio(item.no, "ja")
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor(),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${item.no}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                item.word,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            Spacer(Modifier.height(24.dp))

                            HorizontalDivider()

                            Spacer(Modifier.height(24.dp))

                            FormattedMeaningText(
                                meaning = item.meaning,
                                isMasked = targetIndex == currentIndex && !isMaskRevealed,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (!isMaskRevealed) {
                    Button(
                        onClick = {
                            isMaskRevealed = true
                            onPlayAudio(currentItem.no, "ja")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text(
                            "意味を確認",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 覚えたら押すボタン (左、weight 1f)
                        if (currentItem.isMastered) {
                            FilledTonalButton(
                                onClick = {
                                    onToggleMastered(currentItem, false)
                                    currentItems = currentItems.toMutableList().also {
                                        it[currentIndex] = it[currentIndex].copy(isMastered = false)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
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
                                Text(
                                    "覚えた",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    onToggleMastered(currentItem, true)
                                    currentItems = currentItems.toMutableList().also {
                                        it[currentIndex] = it[currentIndex].copy(isMastered = true)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                )
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text(
                                    "覚えた",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        // 次に進むボタン (右、weight 1.3f)
                        Button(
                            onClick = {
                                currentIndex++
                                isMaskRevealed = false
                                if (currentIndex < currentItems.size) {
                                    onPlayAudio(currentItems[currentIndex].no, "en")
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp),
                        ) {
                            Text(
                                "次に進む",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_forward),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun CardSessionScreenPreview() {
    EnVocabTheme {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentScreen = Screen.Card,
                    onSelectScreen = {}
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                CardSetupScreen(
                    activeBook = VocabBook(id = "1", name = "Forget 810"),
                    items = listOf(
                        VocabItem(id = "1", bookId = "1", no = 1, part = 1, word = "apple", meaning = "[名] りんご", isMastered = false),
                        VocabItem(id = "2", bookId = "1", no = 2, part = 2, word = "banana", meaning = "[名] バナナ", isMastered = true),
                        VocabItem(id = "3", bookId = "1", no = 3, part = 3, word = "cherry", meaning = "[名] さくらんぼ", isMastered = false)
                    ),
                    onStartSession = {},
                    onNavigateToSettings = {}
                )
            }
        }
    }
}
