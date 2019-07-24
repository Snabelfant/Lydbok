package dag.lydbok.util

object DateUtil {
    fun toMmSs(millis: Int) = String.format("%2d:%02d", millis / 60_000, (millis / 1000) % 60)
}
