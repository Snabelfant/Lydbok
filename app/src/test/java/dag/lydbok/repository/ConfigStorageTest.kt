package dag.lydbok.repository

import dag.lydbok.getDurationTest
import dag.lydbok.lydbokDirPaulAuster
import dag.lydbok.model.Position
import dag.lydbok.util.Logger
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File

class ConfigStorageTest {
    @Before
    fun init() {
        Logger.test()
    }

    @Test
    fun test() {
        val configFile = File(lydbokDirPaulAuster, "konfig.json")
        configFile.delete()

        val tracks = TracksBuilder.build(lydbokDirPaulAuster) { file -> file.getDurationTest() }
        val config1 = ConfigStorage.load(lydbokDirPaulAuster, tracks)

        assertThat(config1.currentPosition.track.title, `is`(tracks[0].title))
        assertThat(config1.currentPosition.offset, `is`(0))
        assertThat(config1.isSelected, `is`(false))

        ConfigStorage.save(lydbokDirPaulAuster, config1)
        val config2 = ConfigStorage.load(lydbokDirPaulAuster, tracks)
        assertThat(config2.currentPosition.track, `is`(tracks[0]))
        assertThat(config2.currentPosition.offset, `is`(0))
        assertThat(config2.isSelected, `is`(false))

        config2.currentPosition = Position(tracks[5], 43)
        config2.isSelected = true
        ConfigStorage.save(lydbokDirPaulAuster, config2)

        val config3 = ConfigStorage.load(lydbokDirPaulAuster, tracks)
        assertThat(config3.currentPosition.track, `is`(tracks[5]))
        assertThat(config3.currentPosition.offset, `is`(43))
        assertThat(config3.isSelected, `is`(true))
    }
}
