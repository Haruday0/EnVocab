package io.github.haruday0.envocab.audio

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.haruday0.envocab.data.DataManager
import io.github.haruday0.envocab.model.VocabBook
import io.github.haruday0.envocab.model.VocabItem
import java.util.Locale

interface VocabAudioPlayer {
    fun playAudio(item: VocabItem, lang: String)
    fun playAudio(no: Int, lang: String)
    fun stopAudio()
}

object NoOpVocabAudioPlayer : VocabAudioPlayer {
    override fun playAudio(item: VocabItem, lang: String) {}
    override fun playAudio(no: Int, lang: String) {}
    override fun stopAudio() {}
}

class AndroidVocabAudioPlayer(
    private val context: Context,
    private val activeBookProvider: () -> VocabBook?,
    private val itemsProvider: () -> List<VocabItem>
) : VocabAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var playingItemKey: String? = null

    init {
        try {
            mediaPlayer = MediaPlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val ttsInstance = TextToSpeech(context) { status ->
                if (status != TextToSpeech.SUCCESS) {
                    tts = null
                }
            }
            ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    playingItemKey = null
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    playingItemKey = null
                }
            })
            tts = ttsInstance
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            tts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        playingItemKey = null
    }

    override fun playAudio(item: VocabItem, lang: String) {
        val folderUri = activeBookProvider()?.audioFolderPath.orEmpty()
        val audioUri = DataManager.findAudioUri(context, folderUri, item.no, lang)
        val key = "${item.no}_$lang"

        if (playingItemKey == key) {
            stopAudio()
            return
        }

        stopAudio()

        if (audioUri != null && mediaPlayer != null) {
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(context, audioUri)
                mediaPlayer?.prepare()
                mediaPlayer?.setOnCompletionListener { playingItemKey = null }
                mediaPlayer?.start()
                playingItemKey = key
            } catch (e: Exception) {
                e.printStackTrace()
                playingItemKey = null
            }
        } else if (tts != null) {
            try {
                val textToSpeak = if (lang == "ja") item.meaning else item.word
                val locale = if (lang == "ja") Locale.JAPANESE else Locale.US
                tts?.language = locale
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, key)
                playingItemKey = key
            } catch (e: Exception) {
                e.printStackTrace()
                playingItemKey = null
            }
        }
    }

    override fun playAudio(no: Int, lang: String) {
        val item = itemsProvider().find { it.no == no }
        if (item != null) {
            playAudio(item, lang)
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun rememberVocabAudioPlayer(
    activeBook: () -> VocabBook?,
    items: () -> List<VocabItem>
): VocabAudioPlayer {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        return NoOpVocabAudioPlayer
    }

    val context = LocalContext.current
    val player = remember(context) {
        AndroidVocabAudioPlayer(
            context = context,
            activeBookProvider = activeBook,
            itemsProvider = items
        )
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    return player
}
