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
            config.currentPosition.offset = offset
        }

    var isSelected: Boolean
        get() = config.isSelected
        set(isSelected) {
            config.isSelected = isSelected
        }

    val trackFiles
        get() = tracks.map { it.trackFile.absolutePath }

    val currentTrackFile
        get() = currentTrack.trackFile.absolutePath

    val currentTrackIndex get() = tracks.indexOf(currentTrack)

    fun setCurrentTrack(trackFileName: String) {
        val track = tracks.find { it.trackFile.absolutePath == trackFileName }
        currentTrack = track!!
    }

    override fun toString() = title
}