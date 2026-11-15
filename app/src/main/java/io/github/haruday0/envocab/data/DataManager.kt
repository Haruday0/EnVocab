@file:Suppress("SpellCheckingInspection")

package io.github.haruday0.envocab.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import java.io.File
import java.time.LocalDate

/**
 * データ永続化マネージャー
 * 単語帳、単語データ、検索履歴、設定値をファイルおよび SharedPreferences に保存・取得します。
 */
object DataManager {
    private const val FILE_BOOKS = "vocab_books.json"
    private const val FILE_ITEMS = "vocab_items.json"
    private const val PREFS_NAME = "envocab_prefs"
    private const val KEY_ACTIVE_BOOK = "active_book_id"
    private const val KEY_SEARCH_HISTORY = "search_history"
    private const val KEY_EXAM_DATE = "exam_date"
    private val DEFAULT_EXAM_DATE: LocalDate = LocalDate.of(2027, 1, 16)
    private const val KEY_EXAM_NAME = "exam_name"
    const val DEFAULT_EXAM_NAME = "大学入試共通テスト"

    private const val KEY_CARD_PARTS = "card_selected_parts"
    private const val KEY_CARD_ONLY_UNMASTERED = "card_only_unmastered"
    private const val KEY_CARD_SHUFFLE = "card_shuffle"
    private const val KEY_CARD_QUESTION_COUNT = "card_question_count"
    private const val KEY_CARD_IS_CUSTOM = "card_is_custom"
    private const val KEY_CARD_START_NO = "card_start_no"
    private const val KEY_CARD_END_NO = "card_end_no"

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
        return runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACTIVE_BOOK, null)
        }.getOrNull()
    }

    fun setActiveBookId(context: Context, id: String?) {
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_ACTIVE_BOOK, id)
                }
        }
    }

    fun loadSearchHistory(context: Context): List<String> {
        val json = runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SEARCH_HISTORY, null)
        }.getOrNull() ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSearchHistory(context: Context, history: List<String>) {
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_SEARCH_HISTORY, gson.toJson(history.take(10)))
                }
        }
    }

    /**
     * 試験の日付 (未設定の場合は大学入試共通テスト 2027/01/16)
     */
    fun getExamDate(context: Context): LocalDate {
        return runCatching {
            val epochDay = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_EXAM_DATE, Long.MIN_VALUE)
            if (epochDay == Long.MIN_VALUE) DEFAULT_EXAM_DATE else LocalDate.ofEpochDay(epochDay)
        }.getOrDefault(DEFAULT_EXAM_DATE)
    }

    fun setExamDate(context: Context, date: LocalDate) {
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putLong(KEY_EXAM_DATE, date.toEpochDay())
                }
        }
    }

    fun getExamName(context: Context): String {
        return runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_EXAM_NAME, null) ?: DEFAULT_EXAM_NAME
        }.getOrDefault(DEFAULT_EXAM_NAME)
    }

    fun setExamName(context: Context, name: String) {
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_EXAM_NAME, name)
                }
        }
    }

    data class CardConfig(
        val selectedParts: Set<String> = emptySet(),
        val onlyUnmastered: Boolean = false,
        val isShuffle: Boolean = false,
        val questionCountText: String = "20",
        val isCustomCount: Boolean = false,
        val startNoText: String = "1",
        val endNoText: String = "20",
    )

    fun loadCardConfig(context: Context): CardConfig {
        return runCatching {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val partsJson = prefs.getString(KEY_CARD_PARTS, null)
            val parts: Set<String> = if (partsJson != null) {
                val type = object : TypeToken<Set<String>>() {}.type
                gson.fromJson<Set<String>>(partsJson, type) ?: emptySet()
            } else emptySet()

            val questionCountText = prefs.getString(KEY_CARD_QUESTION_COUNT, "20") ?: "20"
            val startNoText = prefs.getString(KEY_CARD_START_NO, "1")?.ifBlank { "1" } ?: "1"
            val fallbackEndNo = ((startNoText.toIntOrNull() ?: 1) + (questionCountText.toIntOrNull()?.coerceAtLeast(1) ?: 20) - 1).toString()

            CardConfig(
                selectedParts = parts,
                onlyUnmastered = prefs.getBoolean(KEY_CARD_ONLY_UNMASTERED, false),
                isShuffle = prefs.getBoolean(KEY_CARD_SHUFFLE, false),
                questionCountText = questionCountText,
                isCustomCount = prefs.getBoolean(KEY_CARD_IS_CUSTOM, false),
                startNoText = startNoText,
                endNoText = prefs.getString(KEY_CARD_END_NO, fallbackEndNo)?.ifBlank { fallbackEndNo } ?: fallbackEndNo,
            )
        }.getOrDefault(CardConfig())
    }

    fun saveCardConfig(context: Context, config: CardConfig) {
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putString(KEY_CARD_PARTS, gson.toJson(config.selectedParts))
                putBoolean(KEY_CARD_ONLY_UNMASTERED, config.onlyUnmastered)
                putBoolean(KEY_CARD_SHUFFLE, config.isShuffle)
                putString(KEY_CARD_QUESTION_COUNT, config.questionCountText)
                putBoolean(KEY_CARD_IS_CUSTOM, config.isCustomCount)
                val safeStart = config.startNoText.ifBlank { "1" }
                val fallbackEnd = ((safeStart.toIntOrNull() ?: 1) + (config.questionCountText.toIntOrNull()?.coerceAtLeast(1) ?: 20) - 1).toString()
                putString(KEY_CARD_START_NO, safeStart)
                putString(KEY_CARD_END_NO, config.endNoText.ifBlank { fallbackEnd })
            }
        }
    }

    data class ImportResult(
        val addedCount: Int = 0,
        val updatedCount: Int = 0,
    ) {
        val totalProcessed: Int get() = addedCount + updatedCount
    }

    /**
     * CSV またはプレーンテキストを解析して単語リストを生成します。
     */
    fun parseTextData(bookId: String, content: String): List<VocabItem> {
        var currentPart = "1"
        var currentGroup = ""
        val items = mutableListOf<VocabItem>()

        content.lines().forEach { line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("#") && !trimmed.startsWith("##")) {
                val rawHeader = trimmed.removePrefix("#").trim()
                currentPart = if (rawHeader.startsWith("Part", ignoreCase = true)) {
                    val p = rawHeader.removePrefix("Part").removePrefix("part").trim()
                    p.ifEmpty { rawHeader }
                } else {
                    rawHeader
                }.replace("＋", "+")
            } else if (trimmed.startsWith("##")) {
                currentGroup = trimmed.removePrefix("##").trim()
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("//")) {
                val sanitizedLine = trimmed
                    .replace("\uFFFD", "")
                    .replace(Regex("""[\x00-\x1F\x7F]"""), "")

                val parts = sanitizedLine.split(",").map { it.trim() }

                if (parts.size >= 3) {
                    val no = parts[0].toIntOrNull() ?: 0
                    val word = parts[1]
                    val meaning = parts[2]
                    val translation = if (parts.size > 3) parts[3] else ""
                    val note = if (parts.size > 4) parts.subList(4, parts.size).joinToString(", ") else ""

                    items.add(
                        VocabItem(
                            bookId = bookId,
                            no = no,
                            part = currentPart,
                            group = currentGroup,
                            word = word,
                            meaning = meaning,
                            translation = translation,
                            note = note
                        )
                    )
                }
            }
        }
        return items
    }

    /**
     * 既存の単語データと新しい単語データを差分マージします。
     * 同一単語が存在する場合は既存の id と isMastered（習得済みフラグ）を引き継ぎ、
     * CSV に含まれていなかった既存の単語も保持します。
     */
    fun mergeItems(
        existingAllItems: List<VocabItem>,
        newItems: List<VocabItem>,
        bookId: String
    ): Pair<List<VocabItem>, ImportResult> {
        val otherBookItems = existingAllItems.filter { it.bookId != bookId }
        val existingBookItems = existingAllItems.filter { it.bookId == bookId }.toMutableList()

        val mergedBookItems = mutableListOf<VocabItem>()
        var addedCount = 0
        var updatedCount = 0

        for (newItem in newItems) {
            val cleanWord = newItem.word.trim()

            // 優先度1: 単語スペル一致 かつ 番号一致
            val exactMatchIndex = existingBookItems.indexOfFirst {
                it.word.trim().equals(cleanWord, ignoreCase = true) && it.no == newItem.no
            }

            val matchedIndex = if (exactMatchIndex != -1) {
                exactMatchIndex
            } else {
                // 優先度2: 単語スペル一致
                existingBookItems.indexOfFirst {
                    it.word.trim().equals(cleanWord, ignoreCase = true)
                }
            }

            if (matchedIndex != -1) {
                val existing = existingBookItems.removeAt(matchedIndex)
                mergedBookItems.add(
                    newItem.copy(
                        id = existing.id,
                        isMastered = existing.isMastered
                    )
                )
                updatedCount++
            } else {
                mergedBookItems.add(newItem)
                addedCount++
            }
        }

        // CSV に含まれていなかった既存の単語も残す
        mergedBookItems.addAll(existingBookItems)

        val finalItems = otherBookItems + mergedBookItems
        return Pair(finalItems, ImportResult(addedCount = addedCount, updatedCount = updatedCount))
    }

    /**
     * CSV またはプレーンテキストから単語データを差分インポートします。
     * 既存の習得ステータス (isMastered) や CSV にない既存単語は保持されます。
     */
    fun importTextData(context: Context, bookId: String, content: String): ImportResult {
        val parsedItems = parseTextData(bookId, content)
        if (parsedItems.isEmpty()) {
            return ImportResult()
        }

        val allItems = loadItems(context)
        val (updatedAllItems, result) = mergeItems(allItems, parsedItems, bookId)
        saveItems(context, updatedAllItems)

        return result
    }

    /**
     * 単語帳のデータを CSV / プレーンテキスト形式でエクスポートします。
     */
    fun exportTextData(context: Context, bookId: String): String {
        val items = loadItems(context).filter { it.bookId == bookId }
        val sb = StringBuilder()
        val parts = items.groupBy { it.part }
        val sortedPartKeys = parts.keys.sortedWith(Comparator { a, b ->
            val numA = a.toIntOrNull()
            val numB = b.toIntOrNull()
            when {
                numA != null && numB != null -> numA.compareTo(numB)
                numA != null -> -1
                numB != null -> 1
                else -> a.compareTo(b)
            }
        })

        sortedPartKeys.forEach { part ->
            val partItems = parts[part] ?: return@forEach
            val partHeader = if (part.all { it.isDigit() }) "# Part $part" else "# $part"
            sb.appendLine(partHeader)
            val groups = partItems.groupBy { it.group }
            groups.forEach { (group, groupItems) ->
                if (group.isNotBlank()) {
                    sb.appendLine("## $group")
                }
                groupItems.sortedBy { it.no }.forEach { item ->
                    val line = listOf(
                        item.no.toString(),
                        item.word,
                        item.meaning,
                        item.translation,
                        item.note
                    ).joinToString(", ")
                    sb.appendLine(line)
                }
            }
            sb.appendLine()
        }
        return sb.toString().trimEnd()
    }

    /**
     * OpenDocumentTree で選択したディレクトリから音声ファイルの Uri を取得します。
     */
    fun findAudioUri(context: Context, folderUri: String, no: Int, lang: String): Uri? {
        if (folderUri.isBlank()) return null

        val treeUri = runCatching { folderUri.toUri() }.getOrNull() ?: return null
        val documentId = runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull() ?: return null

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            documentId
        )
        val paddedNo = no.toString().padStart(4, '0')
        val names = listOf(
            "${paddedNo}_$lang.mp3",
            "${no}_$lang.mp3"
        )

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )

        return runCatching {
            context.contentResolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

                if (idIndex < 0 || nameIndex < 0) return@use null

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex)
                    if (name in names) {
                        return@use DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            cursor.getString(idIndex)
                        )
                    }
                }
                null
            }
        }.getOrNull()
    }
}
