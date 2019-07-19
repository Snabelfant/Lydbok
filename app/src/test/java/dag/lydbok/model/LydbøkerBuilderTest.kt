package dag.lydbok.model

import dag.lydbok.util.Logger
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class LydbøkerBuilderTest {

    @Test
    fun test() {
        Logger.test()
        val lydbøker = LydbøkerBuilder.build(TestUtil.lydbokDir)
        assertThat(lydbøker.size, `is`(2))
    }
}