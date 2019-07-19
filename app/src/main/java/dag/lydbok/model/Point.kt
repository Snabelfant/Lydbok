package dag.lydbok.model

class Point(val track: Track, val trackOffset: Int, lydbokBaseOffset: Int) {
    val isStartOfTrack = trackOffset == 0
    val lydbokOffset = lydbokBaseOffset + trackOffset

}