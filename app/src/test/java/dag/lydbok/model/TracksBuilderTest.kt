package dag.lydbok.model

import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File

class TracksBuilderTest {

    @Test
    fun test() {
        val lydbokDir = TestUtil.lydbokDirPaulAuster
        val tracks = TracksBuilder.build(lydbokDir)

        assertThat(tracks.size, `is`(30))
        assertThat(tracks.first().title, `is`("Usynlig CD 01/1-03 Usynlig CD 01 Spor 03"))
        assertThat(tracks.first().trackFile, `is`(File(lydbokDir, "Usynlig CD 01\\1-03 Usynlig CD 01 Spor 03.m4a")))
        assertThat(tracks.last().title, `is`("Usynlig CD 07/7-04 Usynlig CD 07 Spor 04"))
        assertThat(tracks.last().trackFile, `is`(File(lydbokDir, "Usynlig CD 07\\7-04 Usynlig CD 07 Spor 04.m4a")))
        assertThat(tracks.duration(), `is`(7_674))

        assertThat(tracks.next(tracks[0]), `is`(tracks[1]))
        assertThat(tracks.next(tracks[1]), `is`(tracks[2]))
        assertThat(tracks.next(tracks[tracks.size - 2]), `is`(tracks.last()))
        assertThat(tracks.next(tracks[tracks.size - 1]), IsNull.nullValue())
    }

}