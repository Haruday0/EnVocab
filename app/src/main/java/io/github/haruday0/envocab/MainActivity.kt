package io.github.haruday0.envocab

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import androidx.compose.ui.tooling.preview.Preview

// ==========================================
// 1. データモデル
// ==========================================
data class VocabBook(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val audioFolderPath: String = "",
    val coverImageFileName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class VocabItem(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val no: Int,
    val part: Int = 1,
    val group: String = "",
    val word: String,
    val meaning: String,
    val translation: String = "",
    val note: String = "",
    val isMastered: Boolean = false
)

// ==========================================
// 2. データ永続化マネージャー (アトミック保存)
// ==========================================
object DataManager {
    private const val FILE_BOOKS = "vocab_books.json"
    private const val FILE_ITEMS = "vocab_items.json"
    private const val PREFS_NAME = "envocab_prefs"
    private const val KEY_ACTIVE_BOOK = "active_book_id"
    private val gson = Gson()

    fun loadBooks(context: Context): MutableList<VocabBook> {
        val file = File(context.filesDir, FILE_BOOKS)
        if (!file.exists()) return mutableListOf()
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<VocabBook>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveBooks(context: Context, books: List<VocabBook>) {
        try {
            val tempFile = File(context.filesDir, "$FILE_BOOKS.tmp")
            val targetFile = File(context.filesDir, FILE_BOOKS)
            tempFile.writeText(gson.toJson(books))
            tempFile.renameTo(targetFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadItems(context: Context): MutableList<VocabItem> {
        val file = File(context.filesDir, FILE_ITEMS)
        if (!file.exists()) return mutableListOf()
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<VocabItem>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveItems(context: Context, items: List<VocabItem>) {
        try {
            val tempFile = File(context.filesDir, "$FILE_ITEMS.tmp")
            val targetFile = File(context.filesDir, FILE_ITEMS)
            tempFile.writeText(gson.toJson(items))
            tempFile.renameTo(targetFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveBookId(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE_BOOK, null)
    }

    fun setActiveBookId(context: Context, id: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_ACTIVE_BOOK, id)
            }
    }

    // CSV / テキストの解析と取り込み
    fun importTextData(context: Context, bookId: String, content: String): Int {
        var currentPart = 1
        var currentGroup = ""
        val newItems = mutableListOf<VocabItem>()

        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("# Part") || trimmed.startsWith("#Part")) {
                currentPart = trimmed.replace(Regex("[^0-9]"), "").toIntOrNull() ?: currentPart
            } else if (trimmed.startsWith("##")) {
                currentGroup = trimmed.removePrefix("##").trim()
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("//")) {
                val parts = trimmed.split(",").map { it.trim() }
                if (parts.size >= 3) {
                    val no = parts[0].toIntOrNull() ?: 0
                    val word = parts[1]
                    val meaning = parts.subList(2, parts.size).joinToString(",")
                    newItems.add(
                        VocabItem(
                            bookId = bookId,
                            no = no,
                            part = currentPart,
                            group = currentGroup,
                            word = word,
                            meaning = meaning
                        )
                    )
                }
            }
        }

        if (newItems.isNotEmpty()) {
            val allItems = loadItems(context)
            allItems.removeAll { it.bookId == bookId } // 上書き更新
            allItems.addAll(newItems)
            saveItems(context, allItems)
        }
        return newItems.size
    }

    // 音声ファイルの安全な検索 (4桁ゼロ埋め または 通常番号)
    fun findAudioFile(folderPath: String, no: Int, lang: String): File? {
        if (folderPath.isBlank()) return null
        val no4 = String.format(Locale.US, "%04d", no)
        val names = listOf("${no4}_$lang.mp3", "${no}_$lang.mp3")
        for (name in names) {
            val f = File(folderPath, name)
            if (f.exists()) return f
        }
        return null
    }
}

// ==========================================
// 3. アプリケーション本体
// ==========================================
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

enum class Screen { Home, Card, List, Settings, AddEditItem }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    // データ状態
    val books = remember { mutableStateListOf<VocabBook>().apply { addAll(DataManager.loadBooks(context)) } }
    val allItems = remember { mutableStateListOf<VocabItem>().apply { addAll(DataManager.loadItems(context)) } }
    var activeBookId by remember { mutableStateOf(DataManager.getActiveBookId(context) ?: books.firstOrNull()?.id) }

    val activeBook = books.find { it.id == activeBookId }
    val currentItems = allItems.filter { it.bookId == activeBookId }

    // 音声再生プレイヤー
    val mediaPlayer = remember { MediaPlayer() }
    var playingItemKey by remember { mutableStateOf<String?>(null) } // "no_lang"

    fun stopAudio() {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        playingItemKey = null
    }

    fun playAudio(no: Int, lang: String) {
        val folder = activeBook?.audioFolderPath ?: ""
        val file = DataManager.findAudioFile(folder, no, lang)
        val key = "${no}_$lang"

        if (playingItemKey == key) {
            stopAudio()
            return
        }

        stopAudio()

        if (file != null && file.exists()) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(file.absolutePath)
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
        onDispose {
            mediaPlayer.release()
        }
    }

    // 単語追加・編集対象
    var editingItem by remember { mutableStateOf<VocabItem?>(null) }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.AddEditItem) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("ホーム") },
                        selected = currentScreen == Screen.Home,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        onClick = { currentScreen = Screen.Home }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Style, null) },
                        label = { Text("カード") },
                        selected = currentScreen == Screen.Card,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        onClick = { currentScreen = Screen.Card }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, null) },
                        label = { Text("リスト") },
                        selected = currentScreen == Screen.List,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        onClick = { currentScreen = Screen.List }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("設定") },
                        selected = currentScreen == Screen.Settings,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        onClick = { currentScreen = Screen.Settings }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    books = books,
                    allItems = allItems,
                    activeBookId = activeBookId,
                    onSelectBook = {
                        activeBookId = it
                        DataManager.setActiveBookId(context, it)
                    },
                    onAddBook = { name ->
                        val newBook = VocabBook(name = name)
                        books.add(newBook)
                        DataManager.saveBooks(context, books)
                        if (activeBookId == null) {
                            activeBookId = newBook.id
                            DataManager.setActiveBookId(context, newBook.id)
                        }
                    },
                    onUpdateBook = { updated ->
                        val idx = books.indexOfFirst { it.id == updated.id }
                        if (idx != -1) {
                            books[idx] = updated
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
                    }
                )

                Screen.Card -> CardSessionScreen(
                    items = currentItems,
                    onPlayAudio = { no, lang -> playAudio(no, lang) },
                    onToggleMastered = { item, isMastered ->
                        val idx = allItems.indexOfFirst { it.id == item.id }
                        if (idx != -1) {
                            allItems[idx] = allItems[idx].copy(isMastered = isMastered)
                            DataManager.saveItems(context, allItems)
                        }
                    }
                )

                Screen.List -> ListScreen(
                    items = currentItems,
                    playingItemKey = playingItemKey,
                    onPlayAudio = { no, lang -> playAudio(no, lang) },
                    onAddItem = {
                        editingItem = null
                        currentScreen = Screen.AddEditItem
                    },
                    onEditItem = { item ->
                        editingItem = item
                        currentScreen = Screen.AddEditItem
                    },
                    onDeleteItem = { item ->
                        allItems.remove(item)
                        DataManager.saveItems(context, allItems)
                    }
                )

                Screen.Settings -> SettingsScreen(
                    books = books,
                    activeBookId = activeBookId,
                    onSelectActiveBook = {
                        activeBookId = it
                        DataManager.setActiveBookId(context, it)
                    }
                )

                Screen.AddEditItem -> AddEditItemScreen(
                    initialItem = editingItem,
                    activeBookId = activeBookId,
                    onBack = { currentScreen = Screen.List },
                    onSave = { savedItem ->
                        val idx = allItems.indexOfFirst { it.id == savedItem.id }
                        if (idx != -1) {
                            allItems[idx] = savedItem
                        } else {
                            allItems.add(savedItem)
                        }
                        DataManager.saveItems(context, allItems)
                        currentScreen = Screen.List
                    }
                )
            }
        }
    }
}

// ==========================================
// 4. ① ホーム画面
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    books: List<VocabBook>,
    allItems: List<VocabItem>,
    activeBookId: String?,
    onSelectBook: (String) -> Unit,
    onAddBook: (String) -> Unit,
    onUpdateBook: (VocabBook) -> Unit,
    onDeleteBook: (VocabBook) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMenuBook by remember { mutableStateOf<VocabBook?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }

    // 表紙画像ピッカー
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && selectedMenuBook != null) {
            val book = selectedMenuBook!!
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            val fileName = "cover_${book.id}.jpg"
            val destFile = File(coversDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            onUpdateBook(book.copy(coverImageFileName = fileName))
            selectedMenuBook = null
        }
    }

    // CSVインポートピッカー
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && selectedMenuBook != null) {
            val book = selectedMenuBook!!
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            val count = DataManager.importTextData(context, book.id, text)
            Toast.makeText(context, "${count}語をインポートしました", Toast.LENGTH_SHORT).show()
            selectedMenuBook = null
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("単語帳", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "作成")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (books.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(56.dp), MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text("単語帳がありません", color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("新しい単語帳を作成")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    val bookItems = allItems.filter { it.bookId == book.id }
                    val total = bookItems.size
                    val mastered = bookItems.count { it.isMastered }
                    val progress = if (total > 0) mastered.toFloat() / total.toFloat() else 0f
                    val isActive = book.id == activeBookId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBook(book.id) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 表紙サムネイル
                            BookCoverThumbnail(
                                context = context,
                                fileName = book.coverImageFileName,
                                modifier = Modifier.size(width = 48.dp, height = 64.dp)
                            )

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = book.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isActive) {
                                        Spacer(Modifier.width(8.dp))
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("選択中", fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "習得 $mastered / $total (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { selectedMenuBook = book }) {
                                Icon(Icons.Default.MoreVert, "メニュー")
                            }
                        }
                    }
                }
            }
        }
    }

    // 単語帳の管理ボトムシート (3点リーダー)
    if (selectedMenuBook != null) {
        val book = selectedMenuBook!!
        ModalBottomSheet(onDismissRequest = { selectedMenuBook = null }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = book.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("CSVからインポート") },
                    leadingContent = { Icon(Icons.Default.Add, null) },
                    modifier = Modifier.clickable {
                        csvPickerLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "*/*"))
                    }
                )
                ListItem(
                    headlineContent = { Text("表紙画像を設定") },
                    leadingContent = { Icon(Icons.Default.Image, null) },
                    modifier = Modifier.clickable {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text("名前を変更") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.clickable { showRenameDialog = true }
                )
                ListItem(
                    headlineContent = { Text("単語帳を削除") },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.clickable {
                        onDeleteBook(book)
                        selectedMenuBook = null
                    }
                )
            }
        }
    }

    // 新規作成ダイアログ
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新しい単語帳") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("単語帳名 (例: LEAP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddBook(name.trim())
                            showAddDialog = false
                        }
                    }
                ) { Text("作成") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("キャンセル") }
            }
        )
    }

    // 名前変更ダイアログ
    if (showRenameDialog && selectedMenuBook != null) {
        var newName by remember { mutableStateOf(selectedMenuBook!!.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("単語帳の名前を変更") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onUpdateBook(selectedMenuBook!!.copy(name = newName.trim()))
                            showRenameDialog = false
                            selectedMenuBook = null
                        }
                    }
                ) { Text("変更") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("キャンセル") }
            }
        )
    }
}

@Composable
fun BookCoverThumbnail(context: Context, fileName: String, modifier: Modifier = Modifier) {
    if (fileName.isNotBlank()) {
        val file = File(context.filesDir, "covers/$fileName")
        if (file.exists()) {
            val bitmap = remember(fileName) { BitmapFactory.decodeFile(file.absolutePath) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = modifier.clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
                return
            }
        }
    }
    // デフォルトサムネイル
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// ==========================================
// 5. ② カード画面（設定 ➔ フラッシュカード）
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSessionScreen(
    items: List<VocabItem>,
    onPlayAudio: (Int, String) -> Unit,
    onToggleMastered: (VocabItem, Boolean) -> Unit
) {
    var inSession by remember { mutableStateOf(false) }
    var sessionItems by remember { mutableStateOf<List<VocabItem>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isMaskRevealed by remember { mutableStateOf(false) }

    // セッション設定状態
    var selectedPart by remember { mutableIntStateOf(0) } // 0=全体
    var onlyUnmastered by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var questionCount by remember { mutableIntStateOf(20) } // 10, 20, 50, 0=全問

    if (!inSession) {
        // --- 設定画面 ---
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text("カード学習設定", fontWeight = FontWeight.Bold) },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "出題範囲",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPart == 0,
                            onClick = { selectedPart = 0 },
                            label = { Text("全体") }
                        )
                        (1..4).forEach { p ->
                            FilterChip(
                                selected = selectedPart == p,
                                onClick = { selectedPart = p },
                                label = { Text("Part $p") }
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("未習得の単語のみ") },
                            supportingContent = { Text("一度覚えた単語を除外します") },
                            trailingContent = {
                                Switch(checked = onlyUnmastered, onCheckedChange = { onlyUnmastered = it })
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            headlineContent = { Text("シャッフル出題") },
                            supportingContent = { Text("毎回ランダムな順番で出題します") },
                            trailingContent = {
                                Switch(checked = isShuffle, onCheckedChange = { isShuffle = it })
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                Text(
                    "出題数",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 20, 50, 0).forEach { cnt ->
                            FilterChip(
                                selected = questionCount == cnt,
                                onClick = { questionCount = cnt },
                                label = { Text(if (cnt == 0) "全問" else "$cnt 問") }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        var filtered = items.filter {
                            (selectedPart == 0 || it.part == selectedPart) &&
                                    (!onlyUnmastered || !it.isMastered)
                        }
                        if (isShuffle) filtered = filtered.shuffled()
                        if (questionCount > 0) filtered = filtered.take(questionCount)

                        sessionItems = filtered
                        currentIndex = 0
                        isMaskRevealed = false
                        inSession = true
                        if (filtered.isNotEmpty()) {
                            onPlayAudio(filtered[0].no, "en")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = items.isNotEmpty()
                ) {
                    Text("学習を開始")
                }
            }
        }
    } else {
        // --- フラッシュカード実行画面 ---
        BackHandler { inSession = false }

        if (sessionItems.isEmpty() || currentIndex >= sessionItems.size) {
            // 終了画面
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text("学習完了", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { inSession = false }) { Text("終了") }
                }
            }
        } else {
            val currentItem = sessionItems[currentIndex]

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("${currentIndex + 1} / ${sessionItems.size}") },
                        navigationIcon = {
                            IconButton(onClick = { inSession = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No. ${currentItem.no}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(8.dp))
                            Text(currentItem.word, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)

                            Spacer(Modifier.height(24.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(24.dp))

                            // 意味・赤シート部分
                            FormattedMeaningText(
                                meaning = currentItem.meaning,
                                isMasked = !isMaskRevealed,
                                fontSize = 20.sp
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
                            modifier = Modifier.fillMaxWidth().height(56.dp)
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
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Icon(Icons.Default.Close, null)
                                Spacer(Modifier.width(8.dp))
                                Text("未習得")
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
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Icon(Icons.Default.Check, null)
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

// ==========================================
// 6. ③ リスト画面
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    items: List<VocabItem>,
    playingItemKey: String?,
    onPlayAudio: (Int, String) -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (VocabItem) -> Unit,
    onDeleteItem: (VocabItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemForDetail by remember { mutableStateOf<VocabItem?>(null) }

    val filtered = items.filter {
        it.word.contains(searchQuery, true) || it.meaning.contains(searchQuery, true) || it.no.toString().contains(searchQuery)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "単語リスト",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("単語・番号・意味で検索") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, "追加")
            }
        }
    ) { innerPadding ->
        if (filtered.isEmpty()) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), Alignment.Center) {
                Text("単語がありません", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    val isPlaying = playingItemKey == "${item.no}_en"

                    ListItem(
                        leadingContent = {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onPlayAudio(item.no, "en") }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        },
                        headlineContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.no}.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(6.dp))
                                Text(item.word, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        },
                        supportingContent = {
                            FormattedMeaningText(
                                meaning = item.meaning,
                                isMasked = false,
                                fontSize = 14.sp
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { selectedItemForDetail = item }) {
                                Icon(Icons.Default.MoreVert, "詳細")
                            }
                        },
                        modifier = Modifier.clickable { selectedItemForDetail = item }
                    )
                    HorizontalDivider()
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // 単語詳細・編集ボトムシート
    if (selectedItemForDetail != null) {
        val item = selectedItemForDetail!!
        ModalBottomSheet(onDismissRequest = { selectedItemForDetail = null }) {
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        selectedItemForDetail = null
                        onEditItem(item)
                    }) {
                        Icon(Icons.Default.Edit, "編集", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        onDeleteItem(item)
                        selectedItemForDetail = null
                    }) {
                        Icon(Icons.Default.Delete, "削除", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Text("No. ${item.no}  (Part ${item.part} ${if (item.group.isNotBlank()) "/ ${item.group}" else ""})",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(4.dp))
                Text(item.word, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text("意味", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                FormattedMeaningText(meaning = item.meaning, isMasked = false, fontSize = 16.sp)

                if (item.translation.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("日本語訳", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(item.translation, style = MaterialTheme.typography.bodyMedium)
                }

                if (item.note.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("メモ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(item.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

// ==========================================
// 7. ④ 設定画面
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    books: List<VocabBook>,
    activeBookId: String?,
    onSelectActiveBook: (String) -> Unit
) {
    val context = LocalContext.current
    var showBookSelectDialog by remember { mutableStateOf(false) }

    val activeBook = books.find { it.id == activeBookId }

    // フォルダ選択ピッカー
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null && activeBook != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val updated = activeBook.copy(audioFolderPath = uri.toString())
            val allBooks = DataManager.loadBooks(context)
            val idx = allBooks.indexOfFirst { it.id == updated.id }
            if (idx != -1) {
                allBooks[idx] = updated
                DataManager.saveBooks(context, allBooks)
            }
            Toast.makeText(context, "音声フォルダを設定しました", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("設定", fontWeight = FontWeight.Bold) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                "学習環境",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // 1. 使用中の単語帳
                    ListItem(
                        headlineContent = { Text("使用中の単語帳", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(activeBook?.name ?: "未選択") },
                        leadingContent = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Style, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showBookSelectDialog = true }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 2. 音声フォルダ選択
                    ListItem(
                        headlineContent = { Text("音声フォルダ", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Text(
                                if (activeBook?.audioFolderPath.isNullOrBlank()) "未設定 (Documents/EnVocab 等)"
                                else activeBook.audioFolderPath,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingContent = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { folderPicker.launch(null) }
                    )
                }
            }
        }
    }

    // 単語帳選択ダイアログ
    if (showBookSelectDialog) {
        AlertDialog(
            onDismissRequest = { showBookSelectDialog = false },
            title = { Text("単語帳を選択") },
            text = {
                LazyColumn {
                    items(books) { book ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    book.name,
                                    fontWeight = if (book.id == activeBookId) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingContent = {
                                RadioButton(selected = book.id == activeBookId, onClick = null)
                            },
                            modifier = Modifier.clickable {
                                onSelectActiveBook(book.id)
                                showBookSelectDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookSelectDialog = false }) { Text("閉じる") }
            }
        )
    }
}
// ==========================================
// 単語追加・編集画面
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    initialItem: VocabItem?,
    activeBookId: String?,
    onBack: () -> Unit,
    onSave: (VocabItem) -> Unit
) {
    BackHandler(onBack = onBack)

    var noStr by remember { mutableStateOf(initialItem?.no?.toString() ?: "") }
    var partStr by remember { mutableStateOf(initialItem?.part?.toString() ?: "1") }
    var group by remember { mutableStateOf(initialItem?.group ?: "") }
    var word by remember { mutableStateOf(initialItem?.word ?: "") }
    var meaning by remember { mutableStateOf(initialItem?.meaning ?: "") }
    var note by remember { mutableStateOf(initialItem?.note ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialItem == null) "単語を追加" else "単語を編集") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る") }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val no = noStr.toIntOrNull() ?: 0
                            val part = partStr.toIntOrNull() ?: 1
                            if (word.isNotBlank() && activeBookId != null) {
                                val item = initialItem?.copy(
                                    no = no,
                                    part = part,
                                    group = group.trim(),
                                    word = word.trim(),
                                    meaning = meaning.trim(),
                                    note = note.trim()
                                ) ?: VocabItem(
                                    bookId = activeBookId,
                                    no = no,
                                    part = part,
                                    group = group.trim(),
                                    word = word.trim(),
                                    meaning = meaning.trim(),
                                    note = note.trim()
                                )
                                onSave(item)
                            }
                        },
                        enabled = word.isNotBlank()
                    ) { Text("保存", fontWeight = FontWeight.Bold) }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = noStr,
                    onValueChange = { noStr = it },
                    label = { Text("No.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = partStr,
                    onValueChange = { partStr = it },
                    label = { Text("Part") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = group,
                onValueChange = { group = it },
                label = { Text("グループ名 (例: 国・町・外国)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("見出し語 (必須)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                label = { Text("意味 (例: [形] {外国}の)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                maxLines = 4
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("メモ・解説") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
        }
    }
}

// ==========================================
// 品詞バッジ ＆ 赤文字（赤シート）の描画ロジック
// ==========================================
@Composable
fun FormattedMeaningText(
    meaning: String,
    isMasked: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val annotated = buildAnnotatedString {
        var cursor = 0
        val regex = Regex("\\[(.*?)]|\\{(.*?)}")

        regex.findAll(meaning).forEach { match ->
            if (match.range.first > cursor) {
                append(meaning.substring(cursor, match.range.first))
            }

            val pos = match.groups[1]?.value
            val redText = match.groups[2]?.value

            if (pos != null) {
                withStyle(
                    SpanStyle(
                        background = Color.Gray.copy(alpha = 0.2f),
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSize.value * 0.85).sp
                    )
                ) {
                    append(" $pos ")
                }
            } else if (redText != null) {
                if (isMasked) {
                    withStyle(
                        SpanStyle(
                            background = Color.Red,
                            color = Color.Transparent
                        )
                    ) {
                        append(" $redText ")
                    }
                } else {
                    withStyle(
                        SpanStyle(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(redText)
                    }
                }
            }
            cursor = match.range.last + 1
        }
        if (cursor < meaning.length) {
            append(meaning.substring(cursor))
        }
    }

    Text(text = annotated, fontSize = fontSize)
}

// ==========================================
// プレビュー表示用
// ==========================================
@Preview(showBackground = true, locale = "ja")
@Composable
fun VocabCardPreview() {
    EnVocabTheme {
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No. 208", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Text("foreign", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                FormattedMeaningText(
                    meaning = "[形] {外国}の [名] 外国人",
                    isMasked = false,
                    fontSize = 20.sp
                )
            }
        }
    }
}

// ==========================================
// ホーム画面のプレビュー（スマホ枠付き）
// ==========================================
@Preview(showBackground = true, showSystemUi = true, locale = "ja")
@Composable
fun HomeScreenPreview() {
    EnVocabTheme {
        HomeScreen(
            books = listOf(
                VocabBook(id = "1", name = "LEAP"),
                VocabBook(id = "2", name = "ターゲット1900")
            ),
            allItems = listOf(
                VocabItem(id = "1", bookId = "1", no = 1, word = "apple", meaning = "りんご", isMastered = true),
                VocabItem(id = "2", bookId = "1", no = 2, word = "banana", meaning = "バナナ", isMastered = false)
            ),
            activeBookId = "1",
            onSelectBook = {},
            onAddBook = {},
            onUpdateBook = {},
            onDeleteBook = {}
        )
    }
}