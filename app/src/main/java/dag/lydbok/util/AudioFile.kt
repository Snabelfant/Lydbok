package dag.lydbok.util

import android.media.MediaMetadataRetriever
import java.io.File

typealias AudioFileDuration = (File) -> Int

fun File.isAudioFile() = isFile && (name.endsWith(".m4a") || name.endsWith(".mp3"))

fun File.getDuration(): Int {
    val mmr = MediaMetadataRetriever()
    mmr.setDataSource(this.absolutePath)
    val duration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
    mmr.release()
    return duration
}
