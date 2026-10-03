package io.github.haruday0.envocab

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.PredictiveBackContainer
import io.github.haruday0.envocab.ui.screens.AddEditItemScreen
import io.github.haruday0.envocab.ui.screens.CardSessionScreen
import io.github.haruday0.envocab.ui.screens.HomeScreen
import io.github.haruday0.envocab.ui.screens.ListScreen
import io.github.haruday0.envocab.ui.screens.SearchScreen
import io.github.haruday0.envocab.ui.screens.SettingsScreen
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnVocabTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current

    var currentTab by remember { mutableStateOf(Screen.Home) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var isAddEditOpen by remember { mutableStateOf(false) }
    var addEditOpenedFromSearch by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<VocabItem?>(null) }

    val books = remember {
        mutableStateListOf<VocabBook>().apply {
            if (isPreview) {
                add(VocabBook(id = "preview_1", name = "Forget 810"))
            } else {
                addAll(DataManager.loadBooks(context))
            }
        }
    }

    val allItems = remember {
        mutableStateListOf<VocabItem>().apply {
            if (isPreview) {
                addAll(
                    listOf(
                        VocabItem(id = "1", bookId = "preview_1", no = 1, part = 1, group = "名詞", word = "apple", meaning = "[名] りんご", isMastered = false),
                        VocabItem(id = "2", bookId = "preview_1", no = 2, part = 1, group = "動詞", word = "consider", meaning = "[動] 〜をよく考える", isMastered = true),
                        VocabItem(id = "3", bookId = "preview_1", no = 3, part = 1, group = "形容詞", word = "crucial", meaning = "[形] 決定的な", isMastered = false)
                    )
                )
            } else {
                addAll(DataManager.loadItems(context))
            }
        }
    }

    var activeBookId by remember {
        mutableStateOf(
            if (isPreview) "preview_1" else (DataManager.getActiveBookId(context) ?: books.firstOrNull()?.id)
        )
    }

    val activeBook = books.find { it.id == activeBookId }
    val currentItems = allItems.filter { it.bookId == activeBookId }

    val mediaPlayer = if (isPreview) null else remember { MediaPlayer() }
    var playingItemKey by remember { mutableStateOf<String?>(null) }

    fun stopAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) mediaPlayer.stop()
            mediaPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        playingItemKey = null
    }

    fun playAudio(no: Int, lang: String) {
        if (isPreview || mediaPlayer == null) return
        val folderUri = activeBook?.audioFolderPath.orEmpty()
        val audioUri = DataManager.findAudioUri(context, folderUri, no, lang)
        val key = "${no}_$lang"

        if (playingItemKey == key) {
            stopAudio()
            return
        }

        stopAudio()

        if (audioUri != null) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(context, audioUri)
                mediaPlayer.prepare()
                mediaPlayer.setOnCompletionListener { playingItemKey = null }
                mediaPlayer.start()
                playingItemKey = key
            } catch (e: Exception) {
                e.printStackTrace()
                playingItemKey = null
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    BackHandler(enabled = currentTab != Screen.Home && !isSearchOpen && !isAddEditOpen) {
        currentTab = Screen.Home
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentScreen = currentTab,
                    onSelectScreen = { screen ->
                        isSearchOpen = false
                        isAddEditOpen = false
                        currentTab = screen
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 200))
                    },
                    label = "tabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        Screen.Home -> HomeScreen(
                            activeBook = activeBook,
                            currentItems = currentItems,
                            onStartCard = { currentTab = Screen.Card },
                            onNavigateToSettings = { currentTab = Screen.Settings }
                        )

                        Screen.Card -> CardSessionScreen(
                            activeBook = activeBook,
                            items = currentItems,
                            onPlayAudio = ::playAudio,
                            onToggleMastered = { item, isMastered ->
                                val index = allItems.indexOfFirst { it.id == item.id }
                                if (index != -1) {
                                    allItems[index] = allItems[index].copy(isMastered = isMastered)
                                    DataManager.saveItems(context, allItems)
                                }
                            },
                            onNavigateToSettings = { currentTab = Screen.Settings }
                        )

                        Screen.List -> ListScreen(
                            activeBook = activeBook,
                            items = currentItems,
                            playingItemKey = playingItemKey,
                            onPlayAudio = ::playAudio,
                            onAddItem = {
                                editingItem = null
                                addEditOpenedFromSearch = false
                                isAddEditOpen = true
                            },
                            onEditItem = { item ->
                                editingItem = item
                                addEditOpenedFromSearch = false
                                isAddEditOpen = true
                            },
                            onDeleteItem = { item ->
                                allItems.remove(item)
                                DataManager.saveItems(context, allItems)
                            },
                            onNavigateToSettings = { currentTab = Screen.Settings },
                            onNavigateToSearch = { isSearchOpen = true }
                        )

                        Screen.Settings -> SettingsScreen(
                            books = books,
                            activeBookId = activeBookId,
                            onSelectActiveBook = {
                                activeBookId = it
                                DataManager.setActiveBookId(context, it)
                            },
                            onAddBook = { name ->
                                val newBook = VocabBook(name = name)
                                books.add(newBook)
                                DataManager.saveBooks(context, books)
                                if (activeBookId == null) {
                                    activeBookId = newBook.id
                                    DataManager.setActiveBookId(context, activeBookId)
                                }
                            },
                            onUpdateBook = { updatedBook ->
                                val index = books.indexOfFirst { it.id == updatedBook.id }
                                if (index != -1) {
                                    books[index] = updatedBook
                                    DataManager.saveBooks(context, books)
                                }
                            },
                            onDeleteBook = { book ->
                                books.remove(book)
                                allItems.removeAll { it.bookId == book.id }
                                DataManager.saveBooks(context, books)
                                DataManager.saveItems(context, allItems)

                                if (activeBookId == book.id) {
                                    activeBookId = books.firstOrNull()?.id
                                    DataManager.setActiveBookId(context, activeBookId)
                                }
                            },
                            onItemsImported = { importedItems ->
                                allItems.removeAll { it.bookId == activeBookId }
                                allItems.addAll(importedItems)
                                DataManager.saveItems(context, allItems)
                            }
                        )

                        else -> Unit
                    }
                }
            }
        }

        if (isSearchOpen) {
            PredictiveBackContainer(
                onBack = { isSearchOpen = false }
            ) { requestBack ->
                SearchScreen(
                    items = currentItems,
                    playingItemKey = playingItemKey,
                    onPlayAudio = ::playAudio,
                    onEditItem = { item ->
                        editingItem = item
                        addEditOpenedFromSearch = true
                        isAddEditOpen = true
                    },
                    onDeleteItem = { item ->
                        allItems.remove(item)
                        DataManager.saveItems(context, allItems)
                    },
                    onBack = requestBack
                )
            }
        }

        if (isAddEditOpen) {
            PredictiveBackContainer(
                onBack = { isAddEditOpen = false }
            ) { requestBack ->
                AddEditItemScreen(
                    initialItem = editingItem,
                    activeBookId = activeBookId,
                    onBack = requestBack,
                    onSave = { savedItem ->
                        val index = allItems.indexOfFirst { it.id == savedItem.id }
                        if (index != -1) {
                            allItems[index] = savedItem
                        } else {
                            allItems.add(savedItem)
                        }
                        DataManager.saveItems(context, allItems)
                        requestBack()
                    }
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun MainAppPreview() {
    EnVocabTheme {
        MainApp()
    }
}