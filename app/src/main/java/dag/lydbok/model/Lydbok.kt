package dag.lydbok.model

import java.io.File

class Lydbok(
    val title: String,
    val tracks: Tracks,
    val points: Points,
    val config: Config,
    val lydbokDir: File
) {
    val duration = tracks.duration()
    val currentOffset: Int
        get() = with(config.currentPosition) { track.startTime + offset }

    val currentTrack = config.currentPosition.track

    var isSelected: Boolean
        get() = config.isSelected
        set(isSelected) {
            config.isSelected = isSelected
        }

    override fun toString() = title
}