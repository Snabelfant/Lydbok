package dag.lydbok

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import dag.lydbok.audioplayer.AudioPlayerCommands
import dag.lydbok.audioplayer.AudioPlayerService
import dag.lydbok.ui.LydbokUi
import dag.lydbok.ui.PlayerUi
import dag.lydbok.util.Logger
import dag.lydbok.viewmodel.LydbokViewModel

class MainActivity : AppCompatActivity() {
    private var serviceBound = false
    private lateinit var audioPlayerService: AudioPlayerService
    private lateinit var lydbokViewModel: LydbokViewModel
    private lateinit var playerUi: PlayerUi

    private val currentPositionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playerUi.updatePosition(intent.getIntExtra("currentposition", 0), intent.getIntExtra("duration", 0))
        }
    }

    private val playbackCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playerUi.disable()
            lydbokViewModel.setPlayingCompleted()
            Logger.info("Ferdig spilt")
            lydbokViewModel.save()
        }
    }

    private val playbackStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playerUi.disable()
            lydbokViewModel.stopPlaying()
            Logger.info("Stoppet")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as AudioPlayerService.LocalBinder
            audioPlayerService = binder.service
            serviceBound = true
            Logger.info("OnServiceConnected $serviceBound")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.info("OnCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        this.supportActionBar?.apply {
            setLogo(R.drawable.lydbok)
            setDisplayUseLogoEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        AudioPlayerCommands(this).apply {
            registerCurrentPositionReceiver(currentPositionReceiver)
            registerPlaybackCompletedReceiver(playbackCompletedReceiver)
            registerPlaybackStoppedReceiver(playbackStoppedReceiver)
        }

        lydbokViewModel = ViewModelProviders.of(this).get(LydbokViewModel::class.java)
        playerUi = PlayerUi(this)
        LydbokUi.build(this, playerUi, lydbokViewModel)
        val playerIntent = Intent(this, AudioPlayerService::class.java)
        val bound = bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        Logger.info("BindService $bound")
    }

}
