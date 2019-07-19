package dag.lydbok.model

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

object TracksBuilder {

    fun build(lydbokDir: File): Tracks {
        val tracks = mutableListOf<Track>()
        val treeWalk = lydbokDir.walkTopDown()
        var lydbokOffset = 0
        treeWalk
            .asSequence()
            .sorted()
            .filter { it.isAudioFile() }
            .forEach {
                val endTimeExclusive = lydbokOffset + getDuration(it)
                tracks.add(Track(it, lydbokOffset, endTimeExclusive))
                lydbokOffset = endTimeExclusive
            }

        tracks.forEach { println(it) }
        return tracks
    }

    private fun File.isAudioFile() = isFile && (name.endsWith(".m4a") || name.endsWith("mp3"))

    private fun getDuration(audioFile: File): Int {
        val file: AudioFile = AudioFileIO.read(audioFile)
        val header = file.audioHeader
        return header.trackLength
    }

}