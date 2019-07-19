package dag.lydbok.model

class TimeLineBuilder(val tracks: List<Track>) {

    fun build(): TimeLine {
        val points = buildPoints(tracks)
        val lydbokDuration = tracks.sumBy { it.duration }
        return TimeLine(lydbokDuration, points)
    }

    private fun buildPoints(tracks: List<Track>): List<Point> {
        var lydbokBaseOffset = 0
        return tracks.map {
            val point = Point(it, 0, lydbokBaseOffset)
            lydbokBaseOffset += it.duration
            point
        }
    }
}