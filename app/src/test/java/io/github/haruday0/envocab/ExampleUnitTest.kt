package io.github.haruday0.envocab

import io.github.haruday0.envocab.audio.extractJapaneseSpeechText
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.VocabItem
import io.github.haruday0.envocab.ui.screens.buildCardSessionItems
import io.github.haruday0.envocab.ui.screens.computeCardEndNo
import io.github.haruday0.envocab.ui.screens.computeCardStartNo
import io.github.haruday0.envocab.ui.screens.filterCardItems
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](https://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun vocabItem_instantiation() {
        val item = VocabItem(id = "1", bookId = "b1", no = 1, part = "1", word = "test", meaning = "テスト")
        assertEquals("test", item.word)
        assertEquals("1", item.part)
    }

    @Test
    fun vocabItem_plusAlphaPart() {
        val item = VocabItem(id = "2", bookId = "b1", no = 2001, part = "+α", word = "roam", meaning = "[自] {歩き回る}")
        assertEquals("+α", item.part)
    }

    @Test
    fun extractJapaneseSpeechText_redTextOnly() {
        // 品詞タグと赤文字 { ... }、通常の黒文字が混在する場合
        val meaning = "[動] {〜を放棄する}、見捨てる"
        assertEquals("を放棄する", extractJapaneseSpeechText(meaning))
    }

    @Test
    fun extractJapaneseSpeechText_multipleRedTexts() {
        // 複数の赤文字がある場合
        val meaning = "[名] {リンゴ} / [動] {食べる}"
        assertEquals("リンゴ、食べる", extractJapaneseSpeechText(meaning))
    }

    @Test
    fun extractJapaneseSpeechText_fallbackWhenNoRedText() {
        // 赤文字がない場合はタグを除去してフォールバック
        val meaning = "[動] あきらめる"
        assertEquals("あきらめる", extractJapaneseSpeechText(meaning))
    }

    @Test
    fun mergeItems_preservesMasteredStatusAndExistingItems() {
        val existingItems = listOf(
            VocabItem(id = "item1", bookId = "b1", no = 1, part = "1", word = "apple", meaning = "りんご", isMastered = true),
            VocabItem(id = "item2", bookId = "b1", no = 2, part = "1", word = "banana", meaning = "バナナ", isMastered = false),
            VocabItem(id = "other1", bookId = "b2", no = 1, part = "1", word = "car", meaning = "車", isMastered = true)
        )

        val csvContent = """
            # Part 1
            ## 果物
            1, apple, リンゴ（林檎）
            3, orange, オレンジ
        """.trimIndent()

        val parsed = io.github.haruday0.envocab.data.DataManager.parseTextData("b1", csvContent)
        val (merged, result) = io.github.haruday0.envocab.data.DataManager.mergeItems(existingItems, parsed, "b1")

        // b1 のアイテム
        val b1Items = merged.filter { it.bookId == "b1" }
        assertEquals(3, b1Items.size) // apple(更新), orange(新規), banana(既存保持)
        assertEquals(1, result.updatedCount)
        assertEquals(1, result.addedCount)

        // apple の検証: 意味が更新され、isMastered=true と元の id が引き継がれていること
        val apple = b1Items.find { it.word == "apple" }!!
        assertEquals("item1", apple.id)
        assertEquals("リンゴ（林檎）", apple.meaning)
        assertEquals(true, apple.isMastered)
        assertEquals("果物", apple.group)

        // orange の検証: 新規追加
        val orange = b1Items.find { it.word == "orange" }!!
        assertEquals(3, orange.no)
        assertEquals("オレンジ", orange.meaning)
        assertEquals(false, orange.isMastered)

        // banana の検証: CSV にないが保持されていること
        val banana = b1Items.find { it.word == "banana" }!!
        assertEquals("item2", banana.id)
        assertEquals("バナナ", banana.meaning)
        assertEquals(false, banana.isMastered)

        // 他のブック (b2) のアイテムが影響を受けていないこと
        val b2Items = merged.filter { it.bookId == "b2" }
        assertEquals(1, b2Items.size)
        assertEquals("car", b2Items[0].word)
    }

    @Test
    fun cardConfig_defaultStartAndEndNo() {
        val config = DataManager.CardConfig()
        assertEquals("1", config.startNoText)
        assertEquals("20", config.endNoText)
    }

    @Test
    fun computeCardRange_syncsWithQuestionCount() {
        assertEquals(20, computeCardEndNo(startNo = 1, count = 20))
        assertEquals(120, computeCardEndNo(startNo = 101, count = 20))
        assertEquals(181, computeCardStartNo(endNo = 200, count = 20))
        assertEquals(1, computeCardStartNo(endNo = 10, count = 20))
    }

    @Test
    fun buildCardSessionItems_respectsRangeAndShuffleMode() {
        val items = listOf(
            VocabItem(id = "3", bookId = "b1", no = 30, part = "1", word = "c", meaning = "C", isMastered = false),
            VocabItem(id = "1", bookId = "b1", no = 10, part = "1", word = "a", meaning = "A", isMastered = false),
            VocabItem(id = "2", bookId = "b1", no = 20, part = "1", word = "b", meaning = "B", isMastered = true),
            VocabItem(id = "4", bookId = "b1", no = 40, part = "2", word = "d", meaning = "D", isMastered = false),
            VocabItem(id = "5", bookId = "b1", no = 50, part = "2", word = "e", meaning = "E", isMastered = false),
        )

        val filteredRange = filterCardItems(
            items = items,
            selectedParts = setOf("1", "2"),
            onlyUnmastered = false,
            startNo = 20,
            endNo = 40,
        )
        assertEquals(listOf(30, 20, 40), filteredRange.map { it.no })

        val sequentialSession = buildCardSessionItems(
            items = items,
            selectedParts = setOf("1", "2"),
            onlyUnmastered = true,
            startNo = 20,
            endNo = 40,
            isShuffle = false,
            count = 10,
        )
        assertEquals(listOf(30, 40), sequentialSession.map { it.no })

        val shuffledSession = buildCardSessionItems(
            items = items,
            selectedParts = setOf("2"),
            onlyUnmastered = false,
            startNo = 999,
            endNo = 999,
            isShuffle = true,
            count = 10,
        )
        assertEquals(setOf(40, 50), shuffledSession.map { it.no }.toSet())
    }
}
