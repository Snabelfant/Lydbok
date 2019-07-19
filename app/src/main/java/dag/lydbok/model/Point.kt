package dag.lydbok.model

class Point(val track: Track, val trackOffset: Int) {
    val isStartOfTrack = trackOffset == 0
    val lydbokOffset = track.startTime + trackOffset
}