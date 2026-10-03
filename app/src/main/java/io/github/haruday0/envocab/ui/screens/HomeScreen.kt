package io.github.haruday0.envocab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import io.github.haruday0.envocab.model.Screen
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.components.AppBottomNavigationBar
import io.github.haruday0.envocab.ui.components.BookCoverThumbnail
import io.github.haruday0.envocab.ui.components.EmptyBookView
import io.github.haruday0.envocab.ui.components.cardContainerColor
import io.github.haruday0.envocab.ui.components.screenBackgroundColor
import io.github.haruday0.envocab.ui.theme.EnVocabTheme
import io.github.haruday0.envocab.ui.theme.ThemePreviews

@Composable
fun HomeScreen(
    activeBook: VocabBook?,
    currentItems: List<VocabItem>,
    onStartCard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        containerColor = screenBackgroundColor(),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        "ホーム",
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
        } else {
            val total = currentItems.size
            val mastered = currentItems.count { it.isMastered }
            val progress = if (total > 0) mastered.toFloat() / total.toFloat() else 0f

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardContainerColor()
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookCoverThumbnail(
                            context = context,
                            fileName = activeBook.coverImageFileName,
                            modifier = Modifier.size(width = 60.dp, height = 80.dp)
                        )

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeBook.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "習得 $mastered / $total (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (currentItems.isNotEmpty()) {
                    Button(
                        onClick = onStartCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("学習を開始")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor()
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "単語が登録されていません",
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "設定タブからCSVをインポートするか、リスト画面から単語を追加してください。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
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
fun HomeScreenPreview() {
    EnVocabTheme {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentScreen = Screen.Home,
                    onSelectScreen = {}
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                HomeScreen(
                    activeBook = VocabBook(id = "1", name = "Forget 810"),
                    currentItems = listOf(
                        VocabItem(id = "1", bookId = "1", no = 1, word = "apple", meaning = "りんご", isMastered = true),
                        VocabItem(id = "2", bookId = "1", no = 2, word = "banana", meaning = "バナナ", isMastered = false)
                    ),
                    onStartCard = {},
                    onNavigateToSettings = {}
                )
            }
        }
    }
}
