package dag.lydbok.repository

import dag.lydbok.model.Track
import dag.lydbok.model.Tracks
import dag.lydbok.util.AudioFileDuration
import dag.lydbok.util.isAudioFile
import java.io.File


object TracksBuilder {
    fun build(lydbokDir: File, duration: AudioFileDuration): Tracks {
        val tracks = mutableListOf<Track>()
        val treeWalk = lydbokDir.walkTopDown()
        var lydbokOffset = 0
        treeWalk
            .asSequence()
            .sorted()
            .filter { it.isAudioFile() }
            .forEach {
                //                Logger.info("${it.absolutePath}: ${it.getDuration()}")
                val endTimeExclusive = lydbokOffset + duration(it)
                tracks.add(Track(it, lydbokOffset, endTimeExclusive))
                lydbokOffset = endTimeExclusive
            }

        tracks.forEach { println(it) }
        return tracks
    }


}