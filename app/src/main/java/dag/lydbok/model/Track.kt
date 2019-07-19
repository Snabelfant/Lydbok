package dag.lydbok.model

import dag.lydbok.exception.LydbokException
import java.io.File

class Track(val trackFile: File, val duration: Int) : Comparable<Track> {
    val title = trackFile.parentFile.name + "/" + trackFile.name.removeSuffix(".m4a").removeSuffix(".mp3")

    override fun compareTo(other: Track) : Int {
        val compareTo = this.trackFile.compareTo(other.trackFile)
        if (compareTo == 0) {
            throw LydbokException("Like spor: ${trackFile.absolutePath}")
        }

        return compareTo
    }

    override fun toString(): String {
        return "Track($trackFile/$duration)"
    }


}