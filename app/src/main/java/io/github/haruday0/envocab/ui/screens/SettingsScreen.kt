package io.github.haruday0.envocab.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import io.github.haruday0.envocab.ui.components.cardContainerColor
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews
import java.io.File
import java.io.FileOutputStream

@Composable
private fun SettingsCardItem(
    headline: String,
    icon: Painter,
    onClick: () -> Unit,
    index: Int,
    count: Int,
    supporting: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.segmentedColors(containerColor = cardContainerColor()),
        leadingContent = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint
            )
        },
        supportingContent = supporting?.let { text ->
            { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    ) {
        Text(headline)
    }
}

@Composable
fun SettingsScreen(
    books: List<VocabBook>,
    activeBookId: String?,
    onSelectActiveBook: (String) -> Unit,
    onAddBook: (String) -> Unit,
    onUpdateBook: (VocabBook) -> Unit,
    onDeleteBook: (VocabBook) -> Unit,
    onItemsImported: (List<VocabItem>) -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val activeBook = books.find { it.id == activeBookId }

    var showBookSelectDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && activeBook != null) {
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
            val fileName = "cover_${activeBook.id}.jpg"
            val destFile = File(coversDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            onUpdateBook(activeBook.copy(coverImageFileName = fileName))
            Toast.makeText(context, "表紙画像を設定しました", Toast.LENGTH_SHORT).show()
        }
    }

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && activeBook != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            val count = DataManager.importTextData(context, activeBook.id, text)
            onItemsImported(DataManager.loadItems(context).filter { it.bookId == activeBook.id })
            Toast.makeText(context, "${count}語をインポートしました", Toast.LENGTH_SHORT).show()
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null && activeBook != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onUpdateBook(activeBook.copy(audioFolderPath = uri.toString()))
            Toast.makeText(context, "音声フォルダを設定しました", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = screenBackgroundColor(),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("設定", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBackgroundColor(),
                    scrolledContainerColor = screenBackgroundColor()
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("単語帳の選択", style = MaterialTheme.typography.titleMedium)

            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsCardItem(
                    headline = "使用中の単語帳",
                    supporting = activeBook?.name ?: "未作成",
                    icon = painterResource(R.drawable.ic_book_3),
                    onClick = { showBookSelectDialog = true },
                    index = 0,
                    count = 2,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                SettingsCardItem(
                    headline = "新しい単語帳を作成",
                    icon = painterResource(R.drawable.ic_add),
                    onClick = { showAddDialog = true },
                    index = 1,
                    count = 2,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (activeBook != null) {
                Text("「${activeBook.name}」の管理", style = MaterialTheme.typography.titleMedium)

                Column(
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsCardItem(
                        headline = "CSVからインポート",
                        icon = painterResource(R.drawable.ic_add),
                        onClick = {
                            csvPickerLauncher.launch(
                                arrayOf("text/*", "text/comma-separated-values", "*/*")
                            )
                        },
                        index = 0,
                        count = 5
                    )

                    SettingsCardItem(
                        headline = "音声フォルダを選択",
                        supporting = if (activeBook.audioFolderPath.isBlank()) "未設定" else "設定済み",
                        icon = painterResource(R.drawable.ic_folder),
                        onClick = { folderPicker.launch(null) },
                        index = 1,
                        count = 5
                    )

                    SettingsCardItem(
                        headline = "表紙画像を設定",
                        icon = painterResource(R.drawable.ic_image),
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        index = 2,
                        count = 5
                    )

                    SettingsCardItem(
                        headline = "名前を変更",
                        icon = painterResource(R.drawable.ic_edit),
                        onClick = { showRenameDialog = true },
                        index = 3,
                        count = 5
                    )

                    SettingsCardItem(
                        headline = "この単語帳を削除",
                        icon = painterResource(R.drawable.ic_delete),
                        onClick = { showDeleteConfirmDialog = true },
                        index = 4,
                        count = 5
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新しい単語帳を作成") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("単語帳の名前") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddBook(name.trim())
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("作成")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (showRenameDialog && activeBook != null) {
        var name by remember { mutableStateOf(activeBook.name) }

        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("名前を変更") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("単語帳の名前") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onUpdateBook(activeBook.copy(name = name.trim()))
                            showRenameDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    if (showBookSelectDialog) {
        AlertDialog(
            onDismissRequest = { showBookSelectDialog = false },
            title = { Text("単語帳を切り替え") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(books) { book ->
                        val selected = book.id == activeBookId
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectActiveBook(book.id)
                                    showBookSelectDialog = false
                                },
                            leadingContent = {
                                RadioButton(
                                    selected = selected,
                                    onClick = {
                                        onSelectActiveBook(book.id)
                                        showBookSelectDialog = false
                                    }
                                )
                            }
                        ) {
                            Text(
                                book.name,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookSelectDialog = false }) {
                    Text("閉じる")
                }
            }
        )
    }

    if (showDeleteConfirmDialog && activeBook != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("単語帳を削除") },
            text = { Text("「${activeBook.name}」と、登録されているすべての単語データを削除しますか？この操作は取り消せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBook(activeBook)
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@ThemePreviews
@Composable
fun SettingsScreenPreview() {
    EnVocabTheme {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentScreen = Screen.Settings,
                    onSelectScreen = {}
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                SettingsScreen(
                    books = listOf(
                        VocabBook(id = "1", name = "Forget 810"),
                        VocabBook(id = "2", name = "TOEIC 頻出単語")
                    ),
                    activeBookId = "1",
                    onSelectActiveBook = {},
                    onAddBook = {},
                    onUpdateBook = {},
                    onDeleteBook = {},
                    onItemsImported = {}
                )
            }
        }
    }
}
