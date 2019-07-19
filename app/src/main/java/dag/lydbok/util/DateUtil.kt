package dag.lydbok.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtil {
    private val ddMM = DateTimeFormatter.ofPattern("dd.MM")
    private val ddMMyy = DateTimeFormatter.ofPattern("dd.MM.yy")

    fun toDdMm(dateTime: LocalDateTime?) = if (dateTime == null) null else ddMM.format(dateTime)

    fun toMmSs(secs: Int) = String.format("%2d:%02d", secs / 60, secs % 60)

    fun toHhMmSs(secs: Int) = String.format("%4d:%02d:%02d", secs / 3600, secs % 3600 / 60, secs % 3600 % 60)

    fun toDdMmYy(dateTime: LocalDateTime?) = if (dateTime == null) null else ddMMyy.format(dateTime)
}
