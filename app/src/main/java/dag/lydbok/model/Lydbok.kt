package dag.lydbok.model

class Lydbok(
    val title: String,
    val tracks: Tracks,
    val points: Points,
    val config: Config
) {
    val duration = tracks.duration()
}