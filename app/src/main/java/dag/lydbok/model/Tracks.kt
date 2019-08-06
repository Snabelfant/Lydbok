package dag.lydbok.model

typealias Tracks = List<Track>

fun Tracks.duration() = last().endTimeExclusive

fun Tracks.next(track: Track) = if (track === last()) this[0] else this[indexOf(track) + 1]