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
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.fasterxml.jackson.core.type.TypeReference
import dag.lydbok.util.JsonMapper
import dag.lydbok.util.Logger
import java.io.IOException

class AudioPlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private val iBinder = LocalBinder()
    private var currentFileName: String? = null
    private var resumePosition: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private var currentPositionBroadcaster: CurrentPositionBroadcaster? = null
    private var trackFiles: List<String>? = null
    private var currentTrackFile: String? = null
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("becomingNoisyReceiver $intent")
            pauseMedia()
        }
    }

    private val setTrackFilesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopMedia()
            trackFiles =
                JsonMapper().read(intent!!.getStringExtra("trackfiles"), object : TypeReference<List<String>>() {})
            currentTrackFile = intent!!.getStringExtra("currenttrackfile")
            Logger.info("Valg sporliste=${trackFiles!!.size}, $currentTrackFile")
        }
    }

    private val playNewPauseOrResumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newFileName = intent.getStringExtra("filename")

            if (newFileName == currentFileName) {
                log("Ny fil, men samme som spilles nå: $currentFileName")
                pauseOrResumeMedia()
            } else {
                currentFileName = newFileName
                resumePosition = intent.getIntExtra("offset", 0)
                log("playNewPauseOrResume ${currentFileName!!}/$resumePosition")

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
        callStateListener()

        registerReceiver(becomingNoisyReceiver, AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(setTrackFilesReceiver, AudioPlayerCommands.INTENT_IN_TRACKFILES)
        registerReceiver(playNewPauseOrResumeReceiver, AudioPlayerCommands.INTENT_PLAYNEWPAUSEORRESUME)
        registerReceiver(forwardSecsReceiver, AudioPlayerCommands.INTENT_FORWARDSECS)
        registerReceiver(forwardPctReceiver, AudioPlayerCommands.INTENT_FORWARDPCT)
        registerReceiver(backwardSecsReceiver, AudioPlayerCommands.INTENT_BACKWARDSECS)
        registerReceiver(backwardPctReceiver, AudioPlayerCommands.INTENT_BACKWARDPCT)
        registerReceiver(seekToReceiver, AudioPlayerCommands.INTENT_SEEKTO)
    }

    override fun onDestroy() {
        log("OnDestroy")
        super.onDestroy()
        mediaPlayer?.run {
            stopMedia()
            release()
        }

        removeAudioFocus()

        phoneStateListener?.run { telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE) }

        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(playNewPauseOrResumeReceiver)
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

        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer == null) {
                    initMediaPlayer()
                } else {
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.start()
                    }
                }
                mediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(
                0.1f,
                0.1f
            )
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        log("OnError $what/$extra")
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> logE(
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> logE("MEDIA ERROR SERVER DIED $extra")
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> logE("MEDIA ERROR UNKNOWN $extra")
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        log("OnPrepared " + mediaPlayer!!)
        mediaPlayer!!.run {
            if (!isPlaying) {
                seekTo(resumePosition)
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
        log("initMediaplayer ${mediaPlayer}")
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer()

        mediaPlayer!!.let {
            it.setOnCompletionListener(this)
            it.setOnErrorListener(this)
            it.setOnPreparedListener(this)
            it.reset()
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }

        try {
            log("Datasource=" + currentFileName!!)
            mediaPlayer!!.setDataSource(currentFileName)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }

        mediaPlayer!!.prepareAsync()
    }

    private fun seekTo(position: Int) {
        log("Flytt til $position")
        mediaPlayer!!.seekTo(position)
        sendPlaybackStatus()
    }

    private fun forwardSecs(secs: Int) {
        log("ForwardSecs ${mediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition + millis
        log("Forward$millis p=$newPosition")
        if (newPosition + millis < mediaPlayer!!.duration) {
            mediaPlayer!!.seekTo(newPosition)
        }

        sendPlaybackStatus()
    }

    private fun forwardPct(pct: Int) {
        log("+% " + mediaPlayer!!.isPlaying)
        val newPosition =
            mediaPlayer!!.currentPosition + (mediaPlayer!!.duration - mediaPlayer!!.currentPosition) * pct / 100
        log("+%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)

        sendPlaybackStatus()
    }

    private fun backwardSecs(secs: Int) {
        log("-s ${mediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition - millis
        log("-s $millis p=$newPosition")
        if (newPosition >= 0) {
            mediaPlayer!!.seekTo(newPosition)
        }

        sendPlaybackStatus()
    }

    private fun backwardPct(pct: Int) {
        log("-% ${mediaPlayer!!.isPlaying}")
        val newPosition = mediaPlayer!!.currentPosition - mediaPlayer!!.currentPosition * pct / 100
        log("-%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)

        sendPlaybackStatus()
    }

    private fun stopMedia() {
        log("StopMedia ${mediaPlayer}")
        if (mediaPlayer == null) return
        log("StopMedia ${mediaPlayer!!.isPlaying}")
        currentPositionBroadcaster!!.stop()
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }

    }

    private fun pauseOrResumeMedia() {
        log("PauseOrResumeMedia " + mediaPlayer!!.isPlaying)
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
        } else {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
        }
    }

    private fun pauseMedia() {
        log("PauseMedia " + mediaPlayer!!.isPlaying)
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
            log("PauseMedia $resumePosition")
        }
    }

    private fun resumeMedia() {
        log("ResumeMedia " + mediaPlayer!!.isPlaying + "/ " + resumePosition)
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
        }
    }

    private fun callStateListener() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE -> if (mediaPlayer != null) {
                        if (ongoingCall) {
                            ongoingCall = false
                            resumeMedia()
                        }
                    }
                }
            }
        }

        telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }


    private fun registerReceiver(receiver: BroadcastReceiver, action: String) {
        val filter = IntentFilter(action)
        this.registerReceiver(receiver, filter)
    }

    private fun sendPlaybackStatus() {
        val intent = Intent()
        intent.action = AudioPlayerCommands.INTENT_PLAYBACKSTATUS
        intent.putExtra("currentposition", mediaPlayer!!.currentPosition)
        intent.putExtra("playing", mediaPlayer!!.isPlaying)
//        log("Avspilling nå: ${mediaPlayer!!.currentPosition}/${mediaPlayer!!.isPlaying} $currentFileName")

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
