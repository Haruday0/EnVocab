package io.github.haruday0.envocab.model

import java.util.UUID

enum class Screen {
    Home,
    Card,
    List,
    Settings,
    AddEditItem,
    Search
}

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
