package io.github.haruday0.envocab

import io.github.haruday0.envocab.audio.extractJapaneseSpeechText
import io.github.haruday0.envocab.model.VocabItem
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
}
