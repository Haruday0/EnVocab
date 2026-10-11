package io.github.haruday0.envocab

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
        val item = VocabItem(id = "1", bookId = "b1", no = 1, word = "test", meaning = "テスト")
        assertEquals("test", item.word)
    }
}
