package io.github.haruday0.envocab

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.haruday0.envocab.model.VocabItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.github.haruday0.envocab", appContext.packageName)
    }

    @Test
    fun vocabItem_instantiation() {
        val item = VocabItem(id = "1", bookId = "b1", no = 1, word = "test", meaning = "テスト")
        assertEquals("test", item.word)
    }
}
