package dag.lydbok

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

val lydbokDir = File("C:\\Users\\Dag\\AndroidStudioProjects\\Lydbok\\app\\src\\test\\resources")
val lydbokDirPaulAuster = File(lydbokDir, "Paul Auster")

fun File.getDurationTest(): Int {
    val file: AudioFile = AudioFileIO.read(this)
    val header = file.audioHeader
    return header.trackLength * 1000
}
