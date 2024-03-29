package dag.lydbok.model

import java.io.File

class Track(val trackFile: File, val startTime: Int, val endTimeExclusive: Int) : Comparable<Track> {
    val title = trackFile.parentFile.name + "/" + trackFile.name.removeSuffix(".m4a").removeSuffix(".mp3")
    val duration = endTimeExclusive - startTime
    fun isAtTime(time: Int) = time in startTime until endTimeExclusive

    override fun compareTo(other: Track): Int {
        val compareTo = this.trackFile.compareTo(other.trackFile)
        if (compareTo == 0) {
            throw LydbokException("Like spor: ${trackFile.absolutePath}")
        }

        return compareTo
    }

    fun isBefore(another: Track) = startTime < another.startTime
    fun isAfter(another: Track) = startTime > another.startTime

    override fun toString(): String {
        return "Track($trackFile/$duration/$startTime/$endTimeExclusive)"
    }


}