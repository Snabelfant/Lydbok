package dag.lydbok.audioplayer

import android.media.MediaPlayer
import dag.lydbok.util.Logger
import java.util.*


typealias CurrentPositionCallback = (currentPosition: Int, isPlaying: Boolean) -> Unit
typealias NewTrackCallback = (fileName: String) -> Unit

object XMediaPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var trackFiles: List<String>
    private lateinit var currentTrack: String
    private var resumePosition = 0
    private lateinit var currentPositionBroadcaster: CurrentPositionBroadcaster
    private lateinit var currentPositionCallback: CurrentPositionCallback
    private lateinit var newTrackCallback: NewTrackCallback

    fun prepare(
        trackFiles: List<String>,
        currentTrack: String,
        resumePosition: Int,
        currentPositionCallback: CurrentPositionCallback,
        newTrackCallback: NewTrackCallback
    ) {
        this.trackFiles = trackFiles
        this.currentTrack = currentTrack
        this.resumePosition = resumePosition
        currentPositionBroadcaster = CurrentPositionBroadcaster(1000, currentPositionCallback)
        currentPositionCallback(resumePosition, false)
        this.currentPositionCallback = currentPositionCallback
        this.newTrackCallback = newTrackCallback
        prepareTrack(false)
    }

    fun pauseOrResume() {
        log("PauseOrResume")

        if (mediaPlayer == null || !mediaPlayer!!.isPlaying) {
            resume()
        } else {
            pause()
        }
    }

    private fun resume() {
        log("Resume ")
        prepareTrack(true)
    }

    fun forwardSecs(secs: Int) {
        log("ForwardSecs $secs")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition + millis
        log("Forward $millis p=$newPosition")
        if (newPosition + millis < mediaPlayer!!.duration) {
            mediaPlayer!!.seekTo(newPosition)
            currentPositionCallback(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
        }
    }

    fun forwardPct(pct: Int) {
        val newPosition =
            mediaPlayer!!.currentPosition + (mediaPlayer!!.duration - mediaPlayer!!.currentPosition) * pct / 100
        log("+%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)
        currentPositionCallback(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
    }

    fun backwardSecs(secs: Int) {
        log("-s ${mediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition - millis
        log("-s $millis p=$newPosition")
        if (newPosition >= 0) {
            mediaPlayer!!.seekTo(newPosition)
            currentPositionCallback(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
        }
    }

    fun backwardPct(pct: Int) {
        log("-% ${mediaPlayer!!.isPlaying}")
        val newPosition = mediaPlayer!!.currentPosition - mediaPlayer!!.currentPosition * pct / 100
        log("-%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)
        currentPositionCallback(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
    }


    fun forwardTrack() {
        val nextTrack = findNextTrack()
        log("Neste spor $currentTrack neste $nextTrack")
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

    fun seekTo(position: Int) {
        log("Flytt til $position")
        mediaPlayer!!.seekTo(position)
        currentPositionCallback(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
    }

    fun release() {
        log("Release")
        currentPositionBroadcaster.stop()
        currentPositionCallback(resumePosition, false)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun pause() {
        log("Pause ${mediaPlayer?.isPlaying}")
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
            if (this == trackFiles.size) null else trackFiles[this + 1]
        }

    private fun findPreviousTrack() =
        with(trackFiles.indexOf(currentTrack)) {
            if (this == trackFiles.size) null else trackFiles[this + 1]
        }

    private class CurrentPositionBroadcaster(
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
                            handler(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
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