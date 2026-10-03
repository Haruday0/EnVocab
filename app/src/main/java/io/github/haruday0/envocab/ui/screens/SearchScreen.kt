package io.github.haruday0.envocab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.DeleteVocabItemDialog
import io.github.haruday0.envocab.ui.components.VocabItemDetailSheet
import io.github.haruday0.envocab.ui.components.VocabItemRow
import io.github.haruday0.envocab.ui.components.cardContainerColor
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.components.searchEmptyBadgeShape
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SearchScreen(
    items: List<VocabItem>,
    playingItemKey: String?,
    onPlayAudio: (Int, String) -> Unit,
    onEditItem: (VocabItem) -> Unit,
    onDeleteItem: (VocabItem) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current


    var searchQuery by remember { mutableStateOf("") }
    var selectedItemForDetail by remember { mutableStateOf<VocabItem?>(null) }
    var itemToDelete by remember { mutableStateOf<VocabItem?>(null) }
    var searchHistory by remember { mutableStateOf(DataManager.loadSearchHistory(context)) }

    fun recordSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            val updated = (listOf(trimmed) + searchHistory.filter { it != trimmed }).take(10)
            searchHistory = updated
            DataManager.saveSearchHistory(context, updated)
        }
    }

    fun deleteHistoryItem(query: String) {
        val updated = searchHistory.filter { it != query }
        searchHistory = updated
        DataManager.saveSearchHistory(context, updated)
    }

    fun clearAllHistory() {
        searchHistory = emptyList()
        DataManager.saveSearchHistory(context, emptyList())
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(80.milliseconds)
        focusRequester.requestFocus()
    }

    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            items.filter {
                it.word.contains(searchQuery, ignoreCase = true) ||
                        it.meaning.contains(searchQuery, ignoreCase = true) ||
                        it.no.toString().contains(searchQuery)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = screenBackgroundColor(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBackgroundColor(),
                    scrolledContainerColor = screenBackgroundColor()
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        keyboardController?.hide()
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "戻る"
                        )
                    }
                },
                title = {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            recordSearch(searchQuery)
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "単語を検索",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_close),
                                            contentDescription = "クリア",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize()
        ) {
            when {
                searchQuery.isNotBlank() && filteredItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(searchEmptyBadgeShape())
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_search),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "一致する単語がありません",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                searchQuery.isNotBlank() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    itemsIndexed(
                        items = filteredItems,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        VocabItemRow(
                            item = item,
                            index = index,
                            count = filteredItems.size,
                            isPlaying = playingItemKey == "${item.no}_en",
                            onPlayAudio = { onPlayAudio(item.no, "en") },
                            onClick = {
                                recordSearch(searchQuery)
                                selectedItemForDetail = item
                            }
                        )
                    }
                }
            }

            searchHistory.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "最近の検索",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = { clearAllHistory() }) {
                                Text("履歴を消去")
                            }
                        }
                    }
                    itemsIndexed(
                        items = searchHistory,
                        key = { _, q -> q }
                    ) { index, historyQuery ->
                        SegmentedListItem(
                            onClick = {
                                searchQuery = historyQuery
                                recordSearch(historyQuery)
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = searchHistory.size
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = cardContainerColor()
                            ),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { deleteHistoryItem(historyQuery) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = "削除",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        ) {
                            Text(
                                text = historyQuery,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .clip(searchEmptyBadgeShape())
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "最近の検索結果はありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    }

    VocabItemDetailSheet(
        item = selectedItemForDetail,
        onDismiss = { selectedItemForDetail = null },
        onEdit = {
            selectedItemForDetail = null
            onEditItem(it)
        },
        onDelete = { itemToDelete = it }
    )

    DeleteVocabItemDialog(
        item = itemToDelete,
        onDismiss = { itemToDelete = null },
        onConfirm = {
            onDeleteItem(it)
            selectedItemForDetail = null
            itemToDelete = null
        }
    )
}

@ThemePreviews
@Composable
fun SearchScreenPreview() {
    EnVocabTheme {
        SearchScreen(
            items = listOf(
                VocabItem(id = "1", bookId = "1", no = 1, word = "apple", meaning = "[名] りんご"),
                VocabItem(id = "2", bookId = "1", no = 2, word = "banana", meaning = "[名] バナナ")
            ),
            playingItemKey = null,
            onPlayAudio = { _, _ -> },
            onEditItem = {},
            onDeleteItem = {},
            onBack = {}
        )
    }
}
