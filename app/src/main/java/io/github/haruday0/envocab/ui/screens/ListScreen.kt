package io.github.haruday0.envocab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch

@Composable
fun ListScreen(
    activeBook: VocabBook?,
    items: List<VocabItem>,
    onPlayAudio: (VocabItem, String) -> Unit,
    onToggleMastered: (VocabItem, Boolean) -> Unit = { _, _ -> },
    onAddItem: () -> Unit,
    onEditItem: (VocabItem) -> Unit,
    onDeleteItem: (VocabItem) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    var selectedItemForDetail by remember { mutableStateOf<VocabItem?>(null) }
    var itemToDelete by remember { mutableStateOf<VocabItem?>(null) }

    val availableParts = remember(items) {
        items.map { it.part }.distinct().sorted()
    }
    val groupedItemsByPart = remember(items) {
        items.groupBy { it.part }.mapValues { (_, partItems) ->
            partItems.groupBy { it.group }
        }
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { availableParts.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activeBook?.id) {
        if (availableParts.isNotEmpty()) {
            pagerState.scrollToPage(0)
        }
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
                Column(
                    modifier = Modifier
                        .padding(top = innerPadding.calculateTopPadding())
                        .fillMaxSize()
                ) {
                    if (availableParts.size > 1) {
                        val tabContent = @Composable {
                            availableParts.forEachIndexed { index, part ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = "Part $part",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }

                        if (availableParts.size in 2..4) {
                            PrimaryTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = screenBackgroundColor(),
                                contentColor = MaterialTheme.colorScheme.primary,
                                divider = {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                },
                                tabs = tabContent
                            )
                        } else {
                            PrimaryScrollableTabRow(
                                selectedTabIndex = pagerState.currentPage,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = screenBackgroundColor(),
                                contentColor = MaterialTheme.colorScheme.primary,
                                edgePadding = 16.dp,
                                divider = {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                },
                                tabs = tabContent
                            )
                        }

                        HorizontalPager(
                            state = pagerState,
                            key = { page -> availableParts.getOrNull(page) ?: page },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            val part = availableParts.getOrNull(page) ?: return@HorizontalPager
                            val groupedItems = groupedItemsByPart[part] ?: emptyMap()
                            val listState = rememberSaveable(part, saver = LazyListState.Saver) {
                                LazyListState()
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = 8.dp,
                                    end = 16.dp,
                                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                            ) {
                                groupedItems.forEach { (groupName, groupItems) ->
                                    if (groupName.isNotBlank()) {
                                        stickyHeader(key = "header_${part}_$groupName") {
                                            Surface(
                                                color = screenBackgroundColor(),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 4.dp, top = 12.dp, bottom = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = groupName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    itemsIndexed(
                                        items = groupItems,
                                        key = { _, item -> item.id },
                                        contentType = { _, _ -> "vocab_item" }
                                    ) { index, item ->
                                        VocabItemRow(
                                            item = item,
                                            index = index,
                                            count = groupItems.size,
                                            onClick = { selectedItemForDetail = item }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val singlePart = availableParts.firstOrNull() ?: 1
                        val groupedItems = groupedItemsByPart[singlePart] ?: emptyMap()
                        val singleListState = rememberSaveable(singlePart, saver = LazyListState.Saver) {
                            LazyListState()
                        }

                        LazyColumn(
                            state = singleListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = innerPadding.calculateBottomPadding() + 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                        ) {
                            groupedItems.forEach { (groupName, groupItems) ->
                                if (groupName.isNotBlank()) {
                                    stickyHeader(key = "header_${singlePart}_$groupName") {
                                        Surface(
                                            color = screenBackgroundColor(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 4.dp, top = 12.dp, bottom = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = groupName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                itemsIndexed(
                                    items = groupItems,
                                    key = { _, item -> item.id },
                                    contentType = { _, _ -> "vocab_item" }
                                ) { index, item ->
                                    VocabItemRow(
                                        item = item,
                                        index = index,
                                        count = groupItems.size,
                                        onClick = { selectedItemForDetail = item }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val currentDetailItem = items.find { it.id == selectedItemForDetail?.id } ?: selectedItemForDetail

    VocabItemDetailSheet(
        item = currentDetailItem,
        onDismiss = { selectedItemForDetail = null },
        onEdit = {
            selectedItemForDetail = null
            onEditItem(it)
        },
        onDelete = { itemToDelete = it },
        onToggleMastered = onToggleMastered,
        onPlayAudio = onPlayAudio
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
                        VocabItem(id = "2", bookId = "1", no = 2, part = 1, group = "名詞", word = "banana", meaning = "[名] バナナ"),
                        VocabItem(id = "3", bookId = "1", no = 3, part = 2, group = "動詞", word = "consider", meaning = "[動] 〜をよく考える"),
                        VocabItem(id = "4", bookId = "1", no = 4, part = 2, group = "動詞", word = "speak", meaning = "[動] 話す")
                    ),
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
