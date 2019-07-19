package dag.lydbok.model

import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class LydbokBuilderTest {

    @Test
    fun test() {
        val lydbokDir = TestUtil.lydbokDirPaulAuster
        val lydbok = LydbokBuilder.build(lydbokDir)

        assertThat( lydbok.title, `is`("Paul Auster") )
        assertThat(lydbok.tracks.size, `is`(30))
    }

}