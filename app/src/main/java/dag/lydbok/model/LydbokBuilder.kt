package dag.lydbok.model

import java.io.File

object LydbokBuilder {

    fun build(lydbokDir: File): Lydbok {
        val title = lydbokDir.name
        val tracks = TracksBuilder.build(lydbokDir)
        val points = PointsBuilder.build(tracks)
        val config = Config.load(lydbokDir, tracks)
        return Lydbok(title, tracks, points, config)
    }
}