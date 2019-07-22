package dag.lydbok.repository

import dag.lydbok.getDurationTest
import dag.lydbok.lydbokDir
import dag.lydbok.util.Logger
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class LydbøkerBuilderTest {

    @Test
    fun test() {
        Logger.test()
        val lydbøker = LydbøkerBuilder.build(lydbokDir) { file -> file.getDurationTest() }
        assertThat(lydbøker.size, `is`(2))
    }
}