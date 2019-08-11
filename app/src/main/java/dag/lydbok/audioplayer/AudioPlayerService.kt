package dag.lydbok.audioplayer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import com.fasterxml.jackson.core.type.TypeReference
import dag.lydbok.util.JsonMapper
import dag.lydbok.util.Logger

class AudioPlayerService : Service() {
    private val iBinder = LocalBinder()
    private var xMediaPlayer: XMediaPlayer? = null

    private val currentPositionCallback: CurrentPositionCallback = { currentPosition, isPlaying ->
        val intent = Intent().apply {
            action = AudioPlayerCommands.INTENT_OUT_PLAYBACKSTATUS
            putExtra("currentposition", currentPosition)
            putExtra("playing", isPlaying)
//            log("Avspilling nå: $currentPosition/$isPlaying")
        }

        sendBroadcast(intent)
    }

    private val newTrackCallback: NewTrackCallback = { fileName ->
        val intent = Intent().apply {
            action = AudioPlayerCommands.INTENT_OUT_NEWTRACK
            putExtra("trackfile", fileName)
            log("Ny fil: $fileName")
        }

        sendBroadcast(intent)
    }

    private val setTrackFilesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val trackFiles =
                JsonMapper().read(intent!!.getStringExtra("trackfiles"), object : TypeReference<List<String>>() {})
            val currentTrackFile = intent.getStringExtra("currenttrackfile")
            val resumePosition = intent.getIntExtra("resumeposition", 0)
            Logger.info("Valg sporliste=${trackFiles.size}, $currentTrackFile, $resumePosition")

            xMediaPlayer?.release()
            xMediaPlayer = XMediaPlayer(
                trackFiles,
                currentTrackFile,
                resumePosition,
                currentPositionCallback,
                newTrackCallback
            )
        }
    }


    private val pauseOrResumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            xMediaPlayer?.pauseOrResume()
        }
    }

    private val forwardTrackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log(">> broadcast $intent")
            xMediaPlayer?.forwardTrack()
        }
    }

    private val backwardTrackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("<< broadcast $intent")
            xMediaPlayer?.backwardTrack()
        }
    }

    private val forwardSecsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log(">S broadcast $intent")
            val secs = intent.getIntExtra("secs", 0)
            xMediaPlayer?.forwardSecs(secs)
        }
    }

    private val forwardPctReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log(">% broadcast $intent")
            val pct = intent.getIntExtra("pct", 0)
            xMediaPlayer?.forwardPct(pct)
        }
    }

    private val backwardSecsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("<S broadcast $intent")
            val secs = intent.getIntExtra("secs", 0)
            xMediaPlayer?.backwardSecs(secs)
        }
    }

    private val backwardPctReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("<% broadcast $intent")
            val pct = intent.getIntExtra("pct", 0)
            xMediaPlayer?.backwardPct(pct)
        }
    }

    private val seekToReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log("<-> broadcast $intent")
            val position = intent.getIntExtra("position", 0)
            xMediaPlayer?.seekTo(position)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        log("OnCreate")

        registerReceiver(setTrackFilesReceiver, AudioPlayerCommands.INTENT_IN_TRACKFILES)
        registerReceiver(pauseOrResumeReceiver, AudioPlayerCommands.INTENT_IN_PAUSEORRESUME)
        registerReceiver(forwardSecsReceiver, AudioPlayerCommands.INTENT_IN_FORWARDSECS)
        registerReceiver(forwardPctReceiver, AudioPlayerCommands.INTENT_IN_FORWARDPCT)
        registerReceiver(forwardTrackReceiver, AudioPlayerCommands.INTENT_IN_FORWARDTRACK)
        registerReceiver(backwardSecsReceiver, AudioPlayerCommands.INTENT_IN_BACKWARDSECS)
        registerReceiver(backwardPctReceiver, AudioPlayerCommands.INTENT_IN_BACKWARDPCT)
        registerReceiver(backwardTrackReceiver, AudioPlayerCommands.INTENT_IN_BACKWARDTRACK)
        registerReceiver(seekToReceiver, AudioPlayerCommands.INTENT_IN_SEEKTO)
    }

    override fun onDestroy() {
        log("OnDestroy")
        super.onDestroy()
        xMediaPlayer?.release()

        unregisterReceiver(setTrackFilesReceiver)
        unregisterReceiver(pauseOrResumeReceiver)
        unregisterReceiver(forwardSecsReceiver)
        unregisterReceiver(forwardPctReceiver)
        unregisterReceiver(forwardTrackReceiver)
        unregisterReceiver(backwardSecsReceiver)
        unregisterReceiver(backwardPctReceiver)
        unregisterReceiver(backwardTrackReceiver)
        unregisterReceiver(seekToReceiver)
    }

    private fun registerReceiver(receiver: BroadcastReceiver, action: String) {
        val filter = IntentFilter(action)
        this.registerReceiver(receiver, filter)
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
