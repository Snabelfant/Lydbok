package dag.lydbok.model

typealias Tracks = List<Track>

fun Tracks.duration() = last().endTimeExclusive

fun Tracks.indexOf(track: Track) = this.find { it.title == track.title }!!
