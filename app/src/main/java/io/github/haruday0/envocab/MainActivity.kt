package io.github.haruday0.envocab

import android.os.Bundle
import io.github.haruday0.envocab.audio.rememberVocabAudioPlayer
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import io.github.haruday0.envocab.ui.components.materialFadeThrough
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.screens.AddEditItemScreen
import io.github.haruday0.envocab.ui.screens.CardPlayScreen
import io.github.haruday0.envocab.ui.screens.CardSetupScreen
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = screenBackgroundColor()
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(Screen.Home) }
    var editingItem by remember { mutableStateOf<VocabItem?>(null) }
    var cardSessionItems by remember { mutableStateOf<List<VocabItem>?>(null) }

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

    val audioPlayer = rememberVocabAudioPlayer(
        activeBook = { activeBook },
        items = { currentItems }
    )

    val density = LocalDensity.current
    val slidePx = with(density) { 30.dp.roundToPx() }
    val m3Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val enterSpec = tween<IntOffset>(durationMillis = 300, easing = m3Easing)
    val exitSpec = tween<IntOffset>(durationMillis = 300, easing = m3Easing)
    val fadeSpec = tween<Float>(durationMillis = 300, easing = m3Easing)

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            slideInHorizontally(enterSpec) { slidePx } + fadeIn(fadeSpec)
        },
        exitTransition = {
            slideOutHorizontally(exitSpec) { -slidePx } + fadeOut(fadeSpec)
        },
        popEnterTransition = {
            slideInHorizontally(enterSpec) { -slidePx } + fadeIn(fadeSpec)
        },
        popExitTransition = {
            slideOutHorizontally(exitSpec) { slidePx } + fadeOut(fadeSpec)
        }
    ) {
        composable("main") {
            BackHandler(enabled = currentTab != Screen.Home) {
                currentTab = Screen.Home
            }

            Scaffold(
                containerColor = screenBackgroundColor(),
                bottomBar = {
                    AppBottomNavigationBar(
                        currentScreen = currentTab,
                        onSelectScreen = { screen ->
                            currentTab = screen
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            focusManager.clearFocus()
                        }
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            materialFadeThrough()
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

                            Screen.Card -> CardSetupScreen(
                                activeBook = activeBook,
                                items = currentItems,
                                onStartSession = { itemsToPlay ->
                                    cardSessionItems = itemsToPlay
                                    navController.navigate("card_play")
                                },
                                onNavigateToSettings = { currentTab = Screen.Settings }
                            )

                            Screen.List -> ListScreen(
                                activeBook = activeBook,
                                items = currentItems,
                                onPlayAudio = { item, lang -> audioPlayer.playAudio(item, lang) },
                                onAddItem = {
                                    editingItem = null
                                    navController.navigate("add_edit")
                                },
                                onEditItem = { item ->
                                    editingItem = item
                                    navController.navigate("add_edit")
                                },
                                onDeleteItem = { item ->
                                    allItems.remove(item)
                                    DataManager.saveItems(context, allItems)
                                },
                                onNavigateToSettings = { currentTab = Screen.Settings },
                                onNavigateToSearch = { navController.navigate("search") }
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
                        }
                    }
                }
            }
        }

        // 検索画面 (M3 Forward and backward: Shared Axis X)
        composable("search") {
            SearchScreen(
                items = currentItems,
                onPlayAudio = { item, lang -> audioPlayer.playAudio(item, lang) },
                onToggleMastered = { item, isMastered ->
                    val index = allItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        allItems[index] = allItems[index].copy(isMastered = isMastered)
                        DataManager.saveItems(context, allItems)
                    }
                },
                onEditItem = { item ->
                    editingItem = item
                    navController.navigate("add_edit")
                },
                onDeleteItem = { item ->
                    allItems.remove(item)
                    DataManager.saveItems(context, allItems)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 単語追加・編集画面 (M3 Forward and backward: Shared Axis X)
        composable("add_edit") {
            AddEditItemScreen(
                initialItem = editingItem,
                activeBookId = activeBookId,
                onBack = { navController.popBackStack() },
                onSave = { savedItem ->
                    val index = allItems.indexOfFirst { it.id == savedItem.id }
                    if (index != -1) {
                        allItems[index] = savedItem
                    } else {
                        allItems.add(savedItem)
                    }
                    DataManager.saveItems(context, allItems)
                    navController.popBackStack()
                }
            )
        }

        // カード学習画面 (M3 Forward and backward: Shared Axis X)
        composable("card_play") {
            CardPlayScreen(
                sessionItems = cardSessionItems ?: emptyList(),
                onPlayAudio = { no, lang -> audioPlayer.playAudio(no, lang) },
                onToggleMastered = { item, isMastered ->
                    val index = allItems.indexOfFirst { it.id == item.id }
                    if (index != -1) {
                        allItems[index] = allItems[index].copy(isMastered = isMastered)
                        DataManager.saveItems(context, allItems)
                    }
                },
                onDismiss = { navController.popBackStack() }
            )
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
