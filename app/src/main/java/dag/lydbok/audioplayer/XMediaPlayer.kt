package dag.lydbok.audioplayer

import android.media.MediaPlayer
import dag.lydbok.util.Logger
import java.util.*


typealias CurrentPositionCallback = (currentPosition: Int, isPlaying: Boolean) -> Unit
typealias NewFilePlayingCallback = (fileName: String) -> Unit

object XMediaPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var trackFiles: List<String>
    private lateinit var currentTrack: String
    private var resumePosition = 0
    private lateinit var currentPositionBroadcaster: CurrentPositionBroadcaster
    private lateinit var currentPositionCallback: CurrentPositionCallback
    private lateinit var newFilePlayingCallback: NewFilePlayingCallback

    fun prepare(
        trackFiles: List<String>,
        currentTrack: String,
        resumePosition: Int,
        currentPositionCallback: CurrentPositionCallback,
        newFilePlayingCallback: NewFilePlayingCallback
    ) {
        this.trackFiles = trackFiles
        this.currentTrack = currentTrack
        this.resumePosition = resumePosition
        currentPositionBroadcaster = CurrentPositionBroadcaster(1000, currentPositionCallback)
        currentPositionCallback(resumePosition, false)
        this.currentPositionCallback = currentPositionCallback
        this.newFilePlayingCallback = newFilePlayingCallback
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
        currentPositionBroadcaster.start()
        mediaPlayer!!.start()
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

    fun forwardTrack() {
        findNextTrack()?.let {
            currentTrack = it
            resumePosition = 0
            newFilePlayingCallback(currentTrack)
            prepareTrack(true)
        }
    }

    fun release() {
        log("Release")
        currentPositionBroadcaster.stop()
        mediaPlayer?.release()
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

        if (nextTrack != null) {
            currentTrack = nextTrack
            resumePosition = 0
            newFilePlayingCallback(currentTrack)
            prepareTrack(true)
        } else {
            currentPositionBroadcaster.stop()
            release()
        }
    }

    private fun createOnPreparedListener(startPlaying: Boolean) = MediaPlayer.OnPreparedListener {
        log("OnPrepared " + mediaPlayer)
        mediaPlayer!!.seekTo(resumePosition)
        mediaPlayer!!.start()
        if (!startPlaying)
            mediaPlayer!!.stop()
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

    private class CurrentPositionBroadcaster(
        private val intervalInMsecs: Int,
        private val handler: CurrentPositionCallback
    ) {
        private lateinit var timer: Timer

        fun start() {
            timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    handler(mediaPlayer!!.currentPosition, mediaPlayer!!.isPlaying)
                }
            }

            timer.scheduleAtFixedRate(timerTask, 1000, intervalInMsecs.toLong())
        }

        fun stop() {
            timer.cancel()
            timer.purge()
        }

    }
}