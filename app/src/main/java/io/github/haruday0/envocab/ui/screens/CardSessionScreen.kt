package io.github.haruday0.envocab.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import io.github.haruday0.envocab.ui.components.EmptyBookView
import io.github.haruday0.envocab.ui.components.FormattedMeaningText
import io.github.haruday0.envocab.ui.components.cardContainerColor
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews

@Composable
fun CardSessionScreen(
    activeBook: VocabBook?,
    items: List<VocabItem>,
    onPlayAudio: (Int, String) -> Unit,
    onToggleMastered: (VocabItem, Boolean) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var inSession by remember { mutableStateOf(false) }
    var sessionItems by remember { mutableStateOf<List<VocabItem>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isMaskRevealed by remember { mutableStateOf(false) }
    var selectedPart by remember { mutableIntStateOf(0) }
    var onlyUnmastered by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var questionCountText by remember { mutableStateOf("20") }

    if (!inSession) {
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
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val availableParts = items.map { it.part }.distinct().sorted()

                    Text("出題範囲", style = MaterialTheme.typography.titleMedium)
                    val partOptions = listOf(0) + availableParts
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        partOptions.forEachIndexed { index, part ->
                            SegmentedButton(
                                selected = selectedPart == part,
                                onClick = { selectedPart = part },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = partOptions.size
                                ),
                                modifier = Modifier.weight(1f),
                                label = { Text(if (part == 0) "全体" else "Part $part") }
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
                            supportingContent = { Text("一度覚えた単語を除外します") },
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
                            supportingContent = { Text("毎回ランダムな順番で出題します") },
                            trailingContent = {
                                Switch(checked = isShuffle, onCheckedChange = { isShuffle = it })
                            }
                        ) {
                            Text("シャッフル出題")
                        }
                    }

                    Text("出題数", style = MaterialTheme.typography.titleMedium)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = cardContainerColor()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    val current = questionCountText.toIntOrNull() ?: 0
                                    val next = maxOf(0, current - 10)
                                    questionCountText = next.toString()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_remove),
                                    contentDescription = "-10"
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                BasicTextField(
                                    value = questionCountText,
                                    onValueChange = { value ->
                                        if (value.all(Char::isDigit)) {
                                            questionCountText = value
                                        }
                                    },
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.widthIn(min = 40.dp, max = 100.dp),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.Center) {
                                            if (questionCountText.isEmpty()) {
                                                Text(
                                                    "0",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                                Text(
                                    text = "問",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    val current = questionCountText.toIntOrNull() ?: 0
                                    val next = current + 10
                                    questionCountText = next.toString()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add),
                                    contentDescription = "+10"
                                )
                            }
                        }
                    }

                    Text(
                        text = "0または未入力で全問出題します",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            var filtered = items.filter {
                                (selectedPart == 0 || it.part == selectedPart) &&
                                        (!onlyUnmastered || !it.isMastered)
                            }

                            if (isShuffle) {
                                filtered = filtered.shuffled()
                            }

                            val count = questionCountText.toIntOrNull() ?: 0
                            if (count > 0) {
                                filtered = filtered.take(count)
                            }

                            sessionItems = filtered
                            currentIndex = 0
                            isMaskRevealed = false
                            inSession = true

                            if (filtered.isNotEmpty()) {
                                onPlayAudio(filtered[0].no, "en")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = items.isNotEmpty()
                    ) {
                        Text("学習を開始")
                    }
                }
            }
        }
    } else {
        BackHandler {
            inSession = false
        }

        if (sessionItems.isEmpty() || currentIndex >= sessionItems.size) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "学習完了",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = { inSession = false }) {
                        Text("終了")
                    }
                }
            }
        } else {
            val currentItem = sessionItems[currentIndex]

            Scaffold(
                containerColor = screenBackgroundColor(),
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = screenBackgroundColor(),
                            scrolledContainerColor = screenBackgroundColor()
                        ),
                        title = {
                            Text("${currentIndex + 1} / ${sessionItems.size}")
                        },
                        navigationIcon = {
                            IconButton(onClick = { inSession = false }) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable {
                                if (!isMaskRevealed) {
                                    isMaskRevealed = true
                                    onPlayAudio(currentItem.no, "ja")
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor()
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
                                "No. ${currentItem.no}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                currentItem.word,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(24.dp))

                            HorizontalDivider()

                            Spacer(Modifier.height(24.dp))

                            FormattedMeaningText(
                                meaning = currentItem.meaning,
                                isMasked = !isMaskRevealed,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (!isMaskRevealed) {
                        Button(
                            onClick = {
                                isMaskRevealed = true
                                onPlayAudio(currentItem.no, "ja")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("意味を確認")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onToggleMastered(currentItem, false)
                                    currentIndex++
                                    isMaskRevealed = false
                                    if (currentIndex < sessionItems.size) {
                                        onPlayAudio(sessionItems[currentIndex].no, "en")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("もう一度")
                            }

                            Button(
                                onClick = {
                                    onToggleMastered(currentItem, true)
                                    currentIndex++
                                    isMaskRevealed = false
                                    if (currentIndex < sessionItems.size) {
                                        onPlayAudio(sessionItems[currentIndex].no, "en")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("習得済み")
                            }
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
                CardSessionScreen(
                    activeBook = VocabBook(id = "1", name = "Forget 810"),
                    items = listOf(
                        VocabItem(id = "1", bookId = "1", no = 1, word = "apple", meaning = "[名] りんご", isMastered = false),
                        VocabItem(id = "2", bookId = "1", no = 2, word = "banana", meaning = "[名] バナナ", isMastered = true)
                    ),
                    onPlayAudio = { _, _ -> },
                    onToggleMastered = { _, _ -> },
                    onNavigateToSettings = {}
                )
            }
        }
    }
}
