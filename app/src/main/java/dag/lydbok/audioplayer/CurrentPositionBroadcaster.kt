package dag.lydbok.audioplayer

import java.util.*

class CurrentPositionBroadcaster(private val intervalInMsecs: Int, private val handler: () -> Unit) {
    private var timer: Timer = Timer()

    fun start() {
        val timerTask = object : TimerTask() {
            override fun run() {
                handler()
            }
        }

        timer.scheduleAtFixedRate(timerTask, 1000, intervalInMsecs.toLong())
    }

    fun stop() {
        timer.cancel()
        timer.purge()
    }
}
