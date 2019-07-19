package dag.lydbok.model

import dag.lydbok.exception.LydbokException
import org.hamcrest.core.Is.`is`
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TrackTest {

    @Test
    fun test() {
        val track = Track(File("C:\\x\\y\\boktittel\\CD1\\Spor 14.m4a"), 89)
        assertThat(track.title, `is`("CD1/Spor 14"))
        assertThat(track.duration, `is`(89))
    }

    @Test
    fun compareTo() {
        val track1 = Track(File("C:\\x\\y\\boktittel\\CD1\\Spor 14.m4a"), 89)
        val track2 = Track(File("C:\\x\\y\\boktittel\\CD1\\Spor 15.m4a"), 89)
        val track3 = Track(File("C:\\x\\y\\boktittel\\CD1\\Spor 15.m4a"), 89)
        assertThat(track1.compareTo(track2), `is`(-1))
        assertThat(track2.compareTo(track1), `is`(1))

        try {
            track2.compareTo(track3)
            fail()
        } catch (e: LydbokException) {
            assertTrue(e.message!!.contains("Like"))
        }
    }
}