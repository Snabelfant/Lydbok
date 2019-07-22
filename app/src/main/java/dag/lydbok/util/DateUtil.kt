package dag.lydbok.util

import java.time.format.DateTimeFormatter

object DateUtil {
    private val ddMM = DateTimeFormatter.ofPattern("dd.MM")
    fun toMmSs(millis: Int) = String.format("%2d:%02d", millis / 60_000, millis % 60_000)
}
