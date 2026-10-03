package io.github.haruday0.envocab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import io.github.haruday0.envocab.ui.components.DeleteVocabItemDialog
import io.github.haruday0.envocab.ui.components.EmptyBookView
import io.github.haruday0.envocab.ui.components.VocabItemDetailSheet
import io.github.haruday0.envocab.ui.components.VocabItemRow
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews

@Composable
fun ListScreen(
    activeBook: VocabBook?,
    items: List<VocabItem>,
    playingItemKey: String?,
    onPlayAudio: (Int, String) -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (VocabItem) -> Unit,
    onDeleteItem: (VocabItem) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    var selectedItemForDetail by remember { mutableStateOf<VocabItem?>(null) }
    var itemToDelete by remember { mutableStateOf<VocabItem?>(null) }

    val groupedItems = remember(items) {
        items.groupBy { it.part to it.group }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = screenBackgroundColor(),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("リスト", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "検索"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBackgroundColor(),
                    scrolledContainerColor = screenBackgroundColor()
                )
            )
        },
        floatingActionButton = {
            if (activeBook != null) {
                ExtendedFloatingActionButton(
                    onClick = onAddItem,
                    icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                    text = { Text("追加") }
                )
            }
        }
    ) { innerPadding ->
        when {
            activeBook == null -> {
                EmptyBookView(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToSettings = onNavigateToSettings
                )
            }

            items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("単語がありません", color = MaterialTheme.colorScheme.outline)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        end = 16.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    groupedItems.forEach { (partAndGroup, groupItems) ->
                        val (part, groupName) = partAndGroup

                        item(key = "header_${part}_$groupName") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, top = 16.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Part $part",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (groupName.isNotBlank()) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        itemsIndexed(
                            items = groupItems,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            VocabItemRow(
                                item = item,
                                index = index,
                                count = groupItems.size,
                                isPlaying = playingItemKey == "${item.no}_en",
                                onPlayAudio = { onPlayAudio(item.no, "en") },
                                onClick = { selectedItemForDetail = item }
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
fun ListScreenPreview() {
    EnVocabTheme {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentScreen = Screen.List,
                    onSelectScreen = {}
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                ListScreen(
                    activeBook = VocabBook(id = "1", name = "Forget 810"),
                    items = listOf(
                        VocabItem(id = "1", bookId = "1", no = 1, part = 1, group = "名詞", word = "apple", meaning = "[名] りんご"),
                        VocabItem(id = "2", bookId = "1", no = 2, part = 1, group = "名詞", word = "banana", meaning = "[名] バナナ")
                    ),
                    playingItemKey = null,
                    onPlayAudio = { _, _ -> },
                    onAddItem = {},
                    onEditItem = {},
                    onDeleteItem = {},
                    onNavigateToSettings = {},
                    onNavigateToSearch = {}
                )
            }
        }
    }
}
