package io.github.haruday0.envocab.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.haruday0.envocab.R
import java.io.File

/**
 * 単語帳の表紙サムネイル画像
 */
@Composable
fun BookCoverThumbnail(
    context: Context,
    fileName: String,
    modifier: Modifier = Modifier
) {
    if (fileName.isNotBlank()) {
        val file = File(context.filesDir, "covers/$fileName")
        if (file.exists()) {
            val bitmap = remember(fileName) {
                BitmapFactory.decodeFile(file.absolutePath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = modifier.clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                return
            }
        }
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_book_3),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 単語帳が登録されていないときの案内表示
 */
@Composable
fun EmptyBookView(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "単語帳がありません",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNavigateToSettings) {
                Text("設定で単語帳を作成")
            }
        }
    }
}
