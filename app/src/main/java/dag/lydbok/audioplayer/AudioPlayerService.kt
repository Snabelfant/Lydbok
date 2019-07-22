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
import android.util.Log

import java.io.IOException

class AudioPlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private val iBinder = LocalBinder()
    private var currentFileName: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var resumePosition: Int = 0
    private var audioManager: AudioManager? = null
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null
    private var currentPositionBroadcaster: CurrentPositionBroadcaster? = null
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("becomingNoisyReceiver $intent")
            pauseMedia()
        }
    }


    private val playNewAudioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            currentFileName = intent.getStringExtra("filename")
            log("playNewAudio " + currentFileName!!)

            stopMedia()
            initMediaPlayer()
        }
    }

    private val pauseOrResumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("pauseOrResume/gjenoppta broadcast $intent")
            pauseOrResumeMedia()
        }
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("Stopp $intent")
            stopMedia()
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

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        log("OnCreate")
        currentPositionBroadcaster = CurrentPositionBroadcaster(1000, { sendCurrentPosition() })

        callStateListener()

        registerReceiver(becomingNoisyReceiver, AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(playNewAudioReceiver, AudioPlayerCommands.INTENT_PLAYNEWAUDIO)
        registerReceiver(pauseOrResumeReceiver, AudioPlayerCommands.INTENT_PAUSEORRESUME)
        registerReceiver(stopReceiver, AudioPlayerCommands.INTENT_STOP)
        registerReceiver(forwardSecsReceiver, AudioPlayerCommands.INTENT_FORWARDSECS)
        registerReceiver(forwardPctReceiver, AudioPlayerCommands.INTENT_FORWARDPCT)
        registerReceiver(backwardSecsReceiver, AudioPlayerCommands.INTENT_BACKWARDSECS)
        registerReceiver(backwardPctReceiver, AudioPlayerCommands.INTENT_BACKWARDPCT)
        registerReceiver(seekToReceiver, AudioPlayerCommands.INTENT_SEEKTO)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log("OnStartCommand " + intent.getStringExtra("filename"))
        currentFileName = intent.getStringExtra("filename")

        if (!requestAudioFocus()) {
            stopSelf()
        }

        initMediaPlayer()
        return super.onStartCommand(intent, flags, startId)
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
        unregisterReceiver(playNewAudioReceiver)
        unregisterReceiver(pauseOrResumeReceiver)
        unregisterReceiver(stopReceiver)
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
        log("OnError $what/$mp")
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.e(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.e("MediaPlayer Error", "MEDIA ERROR SERVER DIED $extra")
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.e("MediaPlayer Error", "MEDIA ERROR UNKNOWN $extra")
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        log("OnPrepared " + mediaPlayer!!)
        playMedia()
    }

    private fun requestAudioFocus(): Boolean {
        log("requestAudioFocus")
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager?.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
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
        log("-> $position")
        mediaPlayer!!.seekTo(position)
        sendCurrentPosition()
    }

    private fun forwardSecs(secs: Int) {
        log("ForwardSecs ${mediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition + millis
        log("Forward$millis p=$newPosition")
        if (newPosition + millis < mediaPlayer!!.duration) {
            mediaPlayer!!.seekTo(newPosition)
        }

        sendCurrentPosition()
    }

    private fun forwardPct(pct: Int) {
        log("+% " + mediaPlayer!!.isPlaying)
        val newPosition =
            mediaPlayer!!.currentPosition + (mediaPlayer!!.duration - mediaPlayer!!.currentPosition) * pct / 100
        log("+%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)

        sendCurrentPosition()
    }

    private fun backwardSecs(secs: Int) {
        log("-s ${mediaPlayer!!.isPlaying}")
        val millis = secs * 1000
        val newPosition = mediaPlayer!!.currentPosition - millis
        log("-s $millis p=$newPosition")
        if (newPosition >= 0) {
            mediaPlayer!!.seekTo(newPosition)
        }

        sendCurrentPosition()
    }

    private fun backwardPct(pct: Int) {
        log("-% ${mediaPlayer!!.isPlaying}")
        val newPosition = mediaPlayer!!.currentPosition - mediaPlayer!!.currentPosition * pct / 100
        log("-%$pct p=$newPosition")
        mediaPlayer!!.seekTo(newPosition)

        sendCurrentPosition()
    }

    private fun playMedia() {
        log("PlayMedia ${mediaPlayer!!.isPlaying}")
        mediaPlayer!!.run {
            if (!isPlaying) {
                start()
                currentPositionBroadcaster!!.start()
                sendCurrentPosition()
            }
        }
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

    private fun sendCurrentPosition() {
        val intent = Intent()
        intent.action = AudioPlayerCommands.INTENT_CURRENTPOSITION
        intent.putExtra("currentposition", mediaPlayer!!.currentPosition)
        intent.putExtra("duration", mediaPlayer!!.duration)
        sendBroadcast(intent)
    }

    private fun sendPlaybackCompleted() {
        val intent = Intent()
        intent.action = AudioPlayerCommands.INTENT_COMPLETED
        sendBroadcast(intent)
    }

    private fun log(s: String) {
        Log.i("ZZZ", "AudioPlayerService $s")
    }

    inner class LocalBinder : Binder() {
        val service: AudioPlayerService
            get() = this@AudioPlayerService
    }
}
