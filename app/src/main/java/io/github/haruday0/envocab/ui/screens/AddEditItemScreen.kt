package io.github.haruday0.envocab.ui.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews

@Composable
fun AddEditItemScreen(
    initialItem: VocabItem?,
    activeBookId: String?,
    onBack: () -> Unit,
    onSave: (VocabItem) -> Unit
) {

    var noStr by remember { mutableStateOf(initialItem?.no?.toString() ?: "") }
    var partStr by remember { mutableStateOf(initialItem?.part?.toString() ?: "1") }
    var group by remember { mutableStateOf(initialItem?.group ?: "") }
    var word by remember { mutableStateOf(initialItem?.word ?: "") }
    var meaning by remember { mutableStateOf(initialItem?.meaning ?: "") }
    var note by remember { mutableStateOf(initialItem?.note ?: "") }

    Scaffold(
        containerColor = screenBackgroundColor(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBackgroundColor(),
                    scrolledContainerColor = screenBackgroundColor()
                ),
                title = {
                    Text(if (initialItem == null) "単語を追加" else "単語を編集")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "戻る"
                        )
                    }
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
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = noStr,
                onValueChange = { noStr = it },
                label = { Text("単語番号 (例: 1)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = partStr,
                onValueChange = { partStr = it },
                label = { Text("Part 番号 (例: 1)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp),
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

@ThemePreviews
@Composable
fun AddEditItemScreenPreview() {
    EnVocabTheme {
        AddEditItemScreen(
            initialItem = VocabItem(id = "1", bookId = "1", no = 1, word = "apple", meaning = "[名] りんご"),
            activeBookId = "1",
            onBack = {},
            onSave = {}
        )
    }
}
