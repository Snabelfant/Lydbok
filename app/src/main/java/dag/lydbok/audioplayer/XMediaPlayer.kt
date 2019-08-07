package dag.lydbok.audioplayer

import android.media.MediaPlayer
import dag.lydbok.util.Logger
import java.util.*


typealias CurrentPositionCallback = (currentPosition: Int, isPlaying: Boolean) -> Unit

object XMediaPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var trackFiles: List<String>
    private lateinit var currentTrack: String
    private var resumePosition = 0
    private lateinit var currentPositionBroadcaster: CurrentPositionBroadcaster

    fun prepare(
        trackFiles: List<String>,
        currentTrack: String,
        resumePosition: Int,
        currentPositionCallback: CurrentPositionCallback
    ) {
        this.trackFiles = trackFiles
        this.currentTrack = currentTrack
        this.resumePosition = resumePosition
        currentPositionBroadcaster = CurrentPositionBroadcaster(1000, currentPositionCallback)
        currentPositionCallback(resumePosition, false)
        prepareTrack()
    }

    fun pauseOrResume() {
        mediaPlayer ?: return

        if (mediaPlayer!!.isPlaying) pause() else resume()
    }

    private fun resume() {
        prepareTrack()
        currentPositionBroadcaster.start()
        mediaPlayer!!.start()
    }

    fun release() {
        currentPositionBroadcaster.stop()
        mediaPlayer?.release()
    }

    private fun pause() {
        resumePosition = mediaPlayer!!.currentPosition
        release()
    }

    private fun prepareTrack() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        mediaPlayer!!.apply {
            setOnErrorListener(createOnErrorListener())
            setOnPreparedListener(createOnPreparedListener())
            setOnCompletionListener { }
            reset()
            setDataSource(currentTrack)
            prepareAsync()
        }
    }

    private fun createOnCompletionListener() = MediaPlayer.OnCompletionListener {
        val nextTrack = findNextTrack()
        if (nextTrack != null) {
            currentTrack = nextTrack
            resumePosition = 0
        }

    }

    private fun createOnPreparedListener() = MediaPlayer.OnPreparedListener {
        log("OnPrepared " + mediaPlayer)
        mediaPlayer!!.seekTo(resumePosition)
    }


    private fun createOnErrorListener() = MediaPlayer.OnErrorListener { _: MediaPlayer, what: Int, extra: Int ->
        log("OnError $what/$extra")
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> logE(
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> logE("MEDIA ERROR SERVER DIED $extra")
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> logE("MEDIA ERROR UNKNOWN $extra")
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