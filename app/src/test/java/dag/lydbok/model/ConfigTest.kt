package dag.lydbok.model

import dag.lydbok.util.Logger
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File

class ConfigTest {
    @Before
    fun init() {
        Logger.test()
    }

    @Test
    fun test() {
        val configFile = File(TestUtil.lydbokDirPaulAuster, "konfig.json")
        configFile.delete()

        val tracks = TracksBuilder.build(TestUtil.lydbokDirPaulAuster)
        val config = Config.load(TestUtil.lydbokDirPaulAuster, tracks)

        assertThat(config.currentPosition.track.title, `is`(tracks[0].title))
        assertThat(config.currentPosition.offset, `is`(0))

        config.save(TestUtil.lydbokDirPaulAuster)
        val config2 = Config.load(TestUtil.lydbokDirPaulAuster, tracks)
        assertThat(config.currentPosition.track, `is`(tracks[0]))
        assertThat(config.currentPosition.offset, `is`(0))

        config2.currentPosition = Position(tracks[5], 43)
        config2.save(TestUtil.lydbokDirPaulAuster)
    }
}
