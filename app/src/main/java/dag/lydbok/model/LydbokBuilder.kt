package dag.lydbok.model

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

class LydbokBuilder( private val lydbokDir: File) {
    private val tracks = mutableListOf<Track>()

    fun load(): LydbokBuilder {
        if (loadExistingConfigFile(lydbokDir)) return this

        val treeWalk = lydbokDir.walkTopDown()
        treeWalk.filter { it.isAudioFile()}. forEach { tracks.add(Track(it, getDuration(it))) }
        tracks.sort()
        tracks.forEach { println(it) }
        return this
    }

    private fun File.isAudioFile() = isFile && (name.endsWith(".m4a") || name.endsWith("mp3"))

    private fun getDuration(audioFile: File): Int {
        val file: AudioFile = AudioFileIO.read(audioFile)
        val header = file.audioHeader
        return header.trackLength
    }

    private fun loadExistingConfigFile(topDir : File) = false

    fun build() = Lydbok( lydbokDir.name, tracks )
}