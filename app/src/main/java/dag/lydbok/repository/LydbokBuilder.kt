package dag.lydbok.repository

import dag.lydbok.model.Lydbok
import dag.lydbok.util.AudioFileDuration
import java.io.File

object LydbokBuilder {

    fun build(lydbokDir: File, duration: AudioFileDuration): Lydbok {
        val title = lydbokDir.name
        val tracks = TracksBuilder.build(lydbokDir, duration)
        val points = PointsBuilder.build(tracks)
        val config = ConfigStorage.load(lydbokDir, tracks)
        return Lydbok(title, tracks, points, config, lydbokDir)
    }
}