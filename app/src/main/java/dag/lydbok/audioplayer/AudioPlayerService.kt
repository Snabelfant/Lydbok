package dag.lydbok.audioplayer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.fasterxml.jackson.core.type.TypeReference
import dag.lydbok.util.JsonMapper
import dag.lydbok.util.Logger
import java.io.IOException

class AudioPlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private val iBinder = LocalBinder()
    private var UTcurrentFileName: String? = null
    private var UTresumePosition: Int = 0
    private var UTmediaPlayer: MediaPlayer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var currentPositionBroadcaster: CurrentPositionBroadcaster? = null

    private val setTrackFilesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val trackFiles =
                JsonMapper().read(intent!!.getStringExtra("trackfiles"), object : TypeReference<List<String>>() {})
            val currentTrackFile = intent.getStringExtra("currenttrackfile")
            val resumePosition = intent.getIntExtra("resumeposition")
            Logger.info("Valg sporliste=${trackFiles!!.size}, $currentTrackFile, $resumePosition")

            XMediaPlayer.prepare(trackFiles, currentTrackFile, resumePosition)
            prepareMediaPlayer()
            if (mediaPlayer.isPlaying) mediaPlayer.stop()

            stopMedia()

        }
    }

    private fun prepareMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        mediaPlayer!!.reset()

    }

    private val pauseOrResumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newFileName = intent.getStringExtra("filename")

            if (newFileName == UTcurrentFileName) {
                log("Ny fil, men samme som spilles nå: $UTcurrentFileName")
                pauseOrResumeMedia()
            } else {
                UTcurrentFileName = newFileName
                UTresumePosition = intent.getIntExtra("offset", 0)
                log("playNewPauseOrResume ${UTcurrentFileName!!}/$UTresumePosition")

                stopMedia()
                initMediaPlayer()
            }
        }
    }

    private val forwardSecsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("+s broadcast $intent")
            val secs = intent.getIntExtra("secs", 0)
            forwardSecs(secs)
        }
    }

    private val forwardPctReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("+% broadcast $intent")
            val pct = intent.getIntExtra("pct", 0)
            forwardPct(pct)
        }
    }

    private val backwardSecsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("-s broadcast $intent")
            val secs = intent.getIntExtra("secs", 0)
            backwardSecs(secs)
        }
    }

    private val backwardPctReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("-% broadcast $intent")
            val pct = intent.getIntExtra("pct", 0)
            backwardPct(pct)
        }
    }

    private val seekToReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("-> broadcast $intent")
            val position = intent.getIntExtra("position", 0)
            seekTo(position)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        log("OnCreate")

        registerReceiver(setTrackFilesReceiver, AudioPlayerCommands.INTENT_IN_TRACKFILES)
        registerReceiver(pauseOrResumeReceiver, AudioPlayerCommands.INTENT_PLAYNEWPAUSEORRESUME)
        registerReceiver(forwardSecsReceiver, AudioPlayerCommands.INTENT_FORWARDSECS)
        registerReceiver(forwardPctReceiver, AudioPlayerCommands.INTENT_FORWARDPCT)
        registerReceiver(backwardSecsReceiver, AudioPlayerCommands.INTENT_BACKWARDSECS)
        registerReceiver(backwardPctReceiver, AudioPlayerCommands.INTENT_BACKWARDPCT)
        registerReceiver(seekToReceiver, AudioPlayerCommands.INTENT_SEEKTO)
    }

    override fun onDestroy() {
        log("OnDestroy")


        super.onDestroy()
        mediaPlayer.release()
        UTmediaPlayer?.run {
            stopMedia()
            release()
        }

        removeAudioFocus()

        unregisterReceiver(setTrackFilesReceiver)
        unregisterReceiver(pauseOrResumeReceiver)
        unregisterReceiver(forwardSecsReceiver)
        unregisterReceiver(forwardPctReceiver)
        unregisterReceiver(backwardSecsReceiver)
        unregisterReceiver(backwardPctReceiver)
        unregisterReceiver(seekToReceiver)
    }

    override fun onCompletion(mp: MediaPlayer) {
        log("OnCompletion $mp")
        stopMedia()
        sendPlaybackCompleted()
        stopSelf()
    }

    override fun onAudioFocusChange(focusState: Int) {
        log("onAudioFocusChange $focusState")

        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (UTmediaPlayer == null) {
                    initMediaPlayer()
                } else {
                    if (!UTmediaPlayer!!.isPlaying) {
                        UTmediaPlayer!!.start()
                    }
                }
                UTmediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (UTmediaPlayer!!.isPlaying) UTmediaPlayer!!.stop()
                UTmediaPlayer!!.release()
                UTmediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (UTmediaPlayer!!.isPlaying) UTmediaPlayer!!.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (UTmediaPlayer!!.isPlaying) UTmediaPlayer!!.setVolume(
                0.1f,
                0.1f
            )
        }
    }


    override fun onPrepared(mp: MediaPlayer) {
        log("OnPrepared " + UTmediaPlayer!!)
        UTmediaPlayer!!.run {
            if (!isPlaying) {
                seekTo(UTresumePosition)
                start()
                currentPositionBroadcaster = CurrentPositionBroadcaster(1000) { sendPlaybackStatus() }
                currentPositionBroadcaster!!.start()
                sendPlaybackStatus()
            }
        }
    }

    private fun removeAudioFocus() {
        log("removeAudioFocus")
        audioManager?.abandonAudioFocus(this)
    }

    private fun initMediaPlayer() {
        log("initMediaplayer ${UTmediaPlayer}")
        if (UTmediaPlayer == null)
            UTmediaPlayer = MediaPlayer()

        UTmediaPlayer!!.let {
            it.setOnCompletionListener(this)
            it.setOnErrorListener(this)
            it.setOnPreparedListener(this)
            it.reset()
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        try {
            log("Datasource=" + UTcurrentFileName!!)
            UTmediaPlayer!!.setDataSource(UTcurrentFileName)
        } catch (e: IOException) {
            e.printStackTrace()
            UTmediaPlayer!!.release()
            stopSelf()
        }

        UTmediaPlayer!!.prepareAsync()
    }

    private fun seekTo(position: Int) {
        log("Flytt til $position")
        UTmediaPlayer!!.seekTo(position)
        sendPlaybackStatus()
    }

    private fun forwardSecs(secs: Int) {
        log("ForwardSecs ${UTmediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = UTmediaPlayer!!.currentPosition + millis
        log("Forward$millis p=$newPosition")
        if (newPosition + millis < UTmediaPlayer!!.duration) {
            UTmediaPlayer!!.seekTo(newPosition)
        }

        sendPlaybackStatus()
    }

    private fun forwardPct(pct: Int) {
        log("+% " + UTmediaPlayer!!.isPlaying)
        val newPosition =
            UTmediaPlayer!!.currentPosition + (UTmediaPlayer!!.duration - UTmediaPlayer!!.currentPosition) * pct / 100
        log("+%$pct p=$newPosition")
        UTmediaPlayer!!.seekTo(newPosition)

        sendPlaybackStatus()
    }

    private fun backwardSecs(secs: Int) {
        log("-s ${UTmediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = UTmediaPlayer!!.currentPosition - millis
        log("-s $millis p=$newPosition")
        if (newPosition >= 0) {
            UTmediaPlayer!!.seekTo(newPosition)
        }

        sendPlaybackStatus()
    }

    private fun backwardPct(pct: Int) {
        log("-% ${UTmediaPlayer!!.isPlaying}")
        val newPosition = UTmediaPlayer!!.currentPosition - UTmediaPlayer!!.currentPosition * pct / 100
        log("-%$pct p=$newPosition")
        UTmediaPlayer!!.seekTo(newPosition)

        sendPlaybackStatus()
    }

    private fun stopMedia() {
        log("StopMedia ${UTmediaPlayer}")
        if (UTmediaPlayer == null) return
        log("StopMedia ${UTmediaPlayer!!.isPlaying}")
        currentPositionBroadcaster!!.stop()
        if (UTmediaPlayer!!.isPlaying) {
            UTmediaPlayer!!.stop()
        }

    }

    private fun pauseOrResumeMedia() {
        log("PauseOrResumeMedia " + UTmediaPlayer!!.isPlaying)
        if (UTmediaPlayer!!.isPlaying) {
            UTmediaPlayer!!.pause()
            UTresumePosition = UTmediaPlayer!!.currentPosition
        } else {
            UTmediaPlayer!!.seekTo(UTresumePosition)
            UTmediaPlayer!!.start()
        }
    }

    private fun pauseMedia() {
        log("PauseMedia " + UTmediaPlayer!!.isPlaying)
        if (UTmediaPlayer!!.isPlaying) {
            UTmediaPlayer!!.pause()
            UTresumePosition = UTmediaPlayer!!.currentPosition
            log("PauseMedia $UTresumePosition")
        }
    }

    private fun resumeMedia() {
        log("ResumeMedia " + UTmediaPlayer!!.isPlaying + "/ " + UTresumePosition)
        if (!UTmediaPlayer!!.isPlaying) {
            UTmediaPlayer!!.seekTo(UTresumePosition)
            UTmediaPlayer!!.start()
        }
    }


    private fun registerReceiver(receiver: BroadcastReceiver, action: String) {
        val filter = IntentFilter(action)
        this.registerReceiver(receiver, filter)
    }

    private fun sendPlaybackStatus() {
        val intent = Intent()
        intent.action = AudioPlayerCommands.INTENT_PLAYBACKSTATUS
        intent.putExtra("currentposition", UTmediaPlayer!!.currentPosition)
        intent.putExtra("playing", UTmediaPlayer!!.isPlaying)
//        log("Avspilling nå: ${UTmediaPlayer!!.currentPosition}/${UTmediaPlayer!!.isPlaying} $UTcurrentFileName")

        sendBroadcast(intent)
    }

    private fun sendPlaybackCompleted() {
        val intent = Intent()
        intent.action = AudioPlayerCommands.INTENT_PLAYBACKCOMPLETED
        sendBroadcast(intent)
    }

    private fun log(s: String) {
        Logger.info("AudioPlayerService $s")
    }

    private fun logE(s: String) {
        Logger.error("AudioPlayerService $s")
    }

    inner class LocalBinder : Binder() {
        val service: AudioPlayerService
            get() = this@AudioPlayerService
    }
}
