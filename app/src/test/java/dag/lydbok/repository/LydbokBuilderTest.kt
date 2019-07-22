package dag.lydbok.repository

import dag.lydbok.getDurationTest
import dag.lydbok.lydbokDirPaulAuster
import dag.lydbok.util.Logger
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class LydbokBuilderTest {
    @Before
    fun init() {
        Logger.test()
    }

    @Test
    fun test() {
        val lydbokDir = lydbokDirPaulAuster
        val lydbok = LydbokBuilder.build(lydbokDir) { file -> file.getDurationTest() }

        assertThat( lydbok.title, `is`("Paul Auster") )
        assertThat(lydbok.tracks.size, `is`(30))
    }

}