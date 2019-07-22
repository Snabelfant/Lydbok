package dag.lydbok.repository

import dag.lydbok.model.Point
import dag.lydbok.model.Track

object PointsBuilder {
    fun build(tracks: List<Track>) = tracks.map { Point(it, 0) }
}