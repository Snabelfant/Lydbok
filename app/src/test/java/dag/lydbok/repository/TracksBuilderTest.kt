package dag.lydbok.repository

import dag.lydbok.getDurationTest
import dag.lydbok.lydbokDirPaulAuster
import dag.lydbok.model.duration
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File

class TracksBuilderTest {

    @Test
    fun test() {
        val lydbokDir = lydbokDirPaulAuster
        val tracks = TracksBuilder.build(lydbokDir) { file -> file.getDurationTest() }

        assertThat(tracks.size, `is`(30))
        assertThat(tracks.first().title, `is`("Usynlig CD 01/1-03 Usynlig CD 01 Spor 03"))
        assertThat(tracks.first().trackFile, `is`(File(lydbokDir, "Usynlig CD 01\\1-03 Usynlig CD 01 Spor 03.m4a")))
        assertThat(tracks.last().title, `is`("Usynlig CD 07/7-04 Usynlig CD 07 Spor 04"))
        assertThat(tracks.last().trackFile, `is`(File(lydbokDir, "Usynlig CD 07\\7-04 Usynlig CD 07 Spor 04.m4a")))
        assertThat(tracks.duration(), `is`(7_674_000))
    }
}