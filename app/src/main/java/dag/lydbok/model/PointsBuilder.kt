package dag.lydbok.model

object PointsBuilder {
    fun build(tracks: List<Track>) = tracks.map { Point(it, 0) } as Points
}