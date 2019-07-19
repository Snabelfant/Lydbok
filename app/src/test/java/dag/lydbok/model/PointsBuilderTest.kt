package dag.lydbok.model

import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull
import org.junit.Assert.assertThat
import org.junit.Test

class PointsBuilderTest {

    @Test
    fun test() {
        val tracks = TracksBuilder.build(TestUtil.lydbokDirPaulAuster)
        val points = PointsBuilder.build(tracks)

        assertThat(points.size, `is`(30))
        assertThat(points.nearestBefore(0), IsNull.nullValue())
        assertThat(points.nearestBefore(1), `is`(points[0]))
        assertThat(points.nearestBefore(100_000), `is`(points.last()))

        assertThat(points.nearestAfter(0), `is`(points[1]))
        assertThat(points.nearestAfter(1), `is`(points[1]))
        assertThat(points.nearestAfter(tracks.last().startTime - 1), `is`(points.last()))
        assertThat(points.nearestAfter(tracks.last().startTime), IsNull.nullValue())
        assertThat(points.nearestAfter(100_000), IsNull.nullValue())
    }

}