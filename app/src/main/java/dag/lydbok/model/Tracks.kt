package dag.lydbok.model

typealias Tracks = List<Track>

fun Tracks.duration() = this.last().endTimeExclusive

fun Tracks.next(track: Track) = if (track === this.last()) null else this[this.indexOf(track) + 1]