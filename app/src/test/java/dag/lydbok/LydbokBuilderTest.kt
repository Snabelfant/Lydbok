package dag.lydbok

import dag.lydbok.model.LydbokBuilder
import org.hamcrest.core.Is.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File

class LydbokBuilderTest {

    @Test
    fun test() {
        val lydbokDir = File("C:\\Users\\Dag\\AndroidStudioProjects\\Lydbok\\app\\src\\test\\resources\\Paul Auster")
        val builder = LydbokBuilder(lydbokDir)
        builder.load()
        val lydbok = builder.build()

        assertThat( lydbok.title, `is`("Paul Auster") )
        assertThat(lydbok.tracks.size, `is`(30))
        assertThat(lydbok.tracks.first().title, `is`("1-03 Usynlig CD 01 Spor 03"))
        assertThat(lydbok.tracks.first().trackFile, `is`(File(lydbokDir,"Usynlig CD 01\\1-03 Usynlig CD 01 Spor 03.m4a")))
        assertThat(lydbok.tracks.last().title, `is`("7-04 Usynlig CD 07 Spor 04"))
        assertThat(lydbok.tracks.last().trackFile, `is`(File(lydbokDir,"Usynlig CD 07\\7-04 Usynlig CD 07 Spor 04.m4a")))
    }

}