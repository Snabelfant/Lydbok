package dag.lydbok.audioplayer

import android.media.MediaPlayer
import dag.lydbok.util.Logger
import java.util.*


typealias CurrentPositionCallback = (currentPosition: Int, isPlaying: Boolean) -> Unit
typealias NewTrackCallback = (fileName: String) -> Unit

class XMediaPlayer(
    private val trackFiles: List<String>, private var currentTrack: String, private var resumePosition: Int,
    private val currentPositionCallback: CurrentPositionCallback, private val newTrackCallback: NewTrackCallback
) {
    private val currentPositionBroadcaster: CurrentPositionBroadcaster =
        CurrentPositionBroadcaster(1000, currentPositionCallback)
    private var mediaPlayer: MediaPlayer? = null

    init {
        currentPositionCallback(resumePosition, false)
    }

    fun pauseOrResume() {
        log("||/>")

        if (mediaPlayer == null) {
            resume()
        } else {
            pause()
        }
    }

    private fun resume() {
        log(">")
        prepareTrack(true)
    }

    fun forwardSecs(secs: Int) {
        log(">S $secs")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition + millis
        log(">S $millis p=$newPosition")
        if (newPosition + millis < mediaPlayer!!.duration) {
            mediaPlayer!!.seekTo(newPosition)
            currentPositionCallback(mediaPlayer!!.currentPosition, true)
        }
    }

    fun forwardPct(pct: Int) {
        log(">% $mediaPlayer")
        val newPosition =
            mediaPlayer!!.currentPosition + (mediaPlayer!!.duration - mediaPlayer!!.currentPosition) * pct / 100
        log("+%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)
        currentPositionCallback(mediaPlayer!!.currentPosition, true)
    }

    fun forwardTrack() {
        val nextTrack = findNextTrack()
        log(">> $currentTrack neste $nextTrack")
        nextTrack?.run {
            currentPositionBroadcaster.stop()
            currentTrack = this
            resumePosition = 0
            newTrackCallback(currentTrack)
            prepareTrack(true)
        }
    }

    fun backwardSecs(secs: Int) {
        log("<S $mediaPlayer")

        mediaPlayer?.run {
            val millis = secs * 1000
            val newPosition = this.currentPosition - millis
            log("-s $millis p=$newPosition")
            if (newPosition >= 0) {
                this.seekTo(newPosition)
                currentPositionCallback(this.currentPosition, true)
            }
        }
    }

    fun backwardPct(pct: Int) {
        log("<% $mediaPlayer")
        val newPosition = mediaPlayer!!.currentPosition - mediaPlayer!!.currentPosition * pct / 100
        log("<%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)
        currentPositionCallback(mediaPlayer!!.currentPosition, true)
    }

    fun backwardTrack() {
        val previousTrack = findPreviousTrack()
        log("<< $currentTrack neste $previousTrack")
        previousTrack?.run {
            currentPositionBroadcaster.stop()
            currentTrack = this
            resumePosition = 0
            newTrackCallback(currentTrack)
            prepareTrack(true)
        }
    }

    fun seekTo(position: Int) {
        log("Flytt til $position $mediaPlayer")
        mediaPlayer?.run {
            this.seekTo(position)
            currentPositionCallback(this.currentPosition, true)
        }
    }

    fun release() {
        log("Release $mediaPlayer")

        mediaPlayer?.run {
            currentPositionBroadcaster.stop()
            currentPositionCallback(resumePosition, false)
            this.release()
        }

        mediaPlayer = null
    }

    private fun pause() {
        log("||")
        resumePosition = mediaPlayer!!.currentPosition
        release()
    }

    private fun prepareTrack(startPlaying: Boolean) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        mediaPlayer!!.apply {
            setOnErrorListener(createOnErrorListener())
            setOnPreparedListener(createOnPreparedListener(startPlaying))
            setOnCompletionListener(createOnCompletionListener())
            reset()
            setDataSource(currentTrack)
            log("Prepare $currentTrack")
            prepareAsync()
        }
    }

    private fun createOnCompletionListener() = MediaPlayer.OnCompletionListener {
        val nextTrack = findNextTrack()
        log("Ferdig $currentTrack neste $nextTrack")
        currentPositionBroadcaster.stop()

        if (nextTrack != null) {
            currentTrack = nextTrack
            resumePosition = 0
            newTrackCallback(currentTrack)
            prepareTrack(true)
        } else {
            release()
        }
    }

    private fun createOnPreparedListener(startPlaying: Boolean) = MediaPlayer.OnPreparedListener {
        log("OnPrepared " + mediaPlayer)
        mediaPlayer!!.seekTo(resumePosition)
        currentPositionCallback(resumePosition, false)
        if (startPlaying) {
            currentPositionBroadcaster.start()
            mediaPlayer!!.start()
        }
    }


    private fun createOnErrorListener() = MediaPlayer.OnErrorListener { _: MediaPlayer, what: Int, extra: Int ->
        log("OnError $what/$extra")
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> logE(
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> logE("MEDIA ERROR SERVER DIED $extra")
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> logE("MEDIA ERROR UNKNOWN $extra")
            else -> logE("MediaPlayer OnError $what $extra")
        }
        false
    }

    private fun log(s: String) {
        Logger.info("XMediaPlayer $s")
    }

    private fun logE(s: String) {
        Logger.error("XMediaPlayer $s")
    }

    private fun findNextTrack() =
        with(trackFiles.indexOf(currentTrack)) {
            if (this == trackFiles.lastIndex) null else trackFiles[this + 1]
        }

    private fun findPreviousTrack() =
        with(trackFiles.indexOf(currentTrack)) {
            if (this == 0) null else trackFiles[this - 1]
        }

    private inner class CurrentPositionBroadcaster(
        private val intervalInMsecs: Int,
        private val handler: CurrentPositionCallback
    ) {
        private var timer: Timer? = null

        fun start() {
            log("CP start: ")
            timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    if (mediaPlayer == null) {
                        handler(resumePosition, false)
                    } else {
                        try {
                            handler(mediaPlayer!!.currentPosition, true)
                        } catch (e: IllegalStateException) {
                            logE(e.toString())
                        }
                    }
                }
            }

            timer!!.scheduleAtFixedRate(timerTask, 1000, intervalInMsecs.toLong())
        }

        fun stop() {
            log("CP stop ${timer == null}")
            timer?.cancel()
            timer?.purge()
        }
    }
}