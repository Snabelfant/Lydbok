package dag.lydbok.model

import java.io.File

class Lydbok(
    val title: String,
    val tracks: Tracks,
    val config: Config,
    val lydbokDir: File
) {
    val duration = tracks.duration()

    val currentLydbokOffset: Int
        get() = with(config.currentPosition) { track.startTime + offset }

    var currentTrack
        get() = config.currentPosition.track
        set(track) {
            config.currentPosition = Position(track, 0)
        }

    var currentTrackOffset
        get() = config.currentPosition.offset
        set(offset) {
//            Logger.info("Pos ${config.currentPosition.track.title}=$offset")
            config.currentPosition.offset = offset
        }

    var isSelected: Boolean
        get() = config.isSelected
        set(isSelected) {
            config.isSelected = isSelected
        }

    fun nextTrack() {
        currentTrack = tracks.next(currentTrack)
    }

    val trackFiles
        get() = tracks.map { it.trackFile.absolutePath }
    val currentTrackFile
        get() = currentTrack.trackFile.absolutePath

    override fun toString() = title
}