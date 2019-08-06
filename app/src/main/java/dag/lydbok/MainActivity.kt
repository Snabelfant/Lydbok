package dag.lydbok

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
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

    private val playbackStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            playerUi.updatePlaybackStatus(
                intent.getIntExtra("currentposition", 0),
                intent.getBooleanExtra("playing", false)
            )
        }
    }

    private val playbackCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lydbokViewModel.playNext()
            Logger.info("Ferdig spilt")
            lydbokViewModel.saveAll()
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
            registerPlaybackStatusReceiver(playbackStatusReceiver)
            registerPlaybackCompletedReceiver(playbackCompletedReceiver)
        }

        lydbokViewModel = ViewModelProviders.of(this).get(LydbokViewModel::class.java)
        playerUi = PlayerUi(this, lydbokViewModel)
        LydbokUi.build(this, lydbokViewModel)
        val playerIntent = Intent(this, AudioPlayerService::class.java)
        val bound = bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        Logger.info("BindService $bound")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onPause() {
        Logger.info("Pause")
        super.onPause()
        lydbokViewModel.saveAll()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("serviceStatus")
    }


    override fun onDestroy() {
        super.onDestroy()
        Logger.info("Destroy $serviceBound")
        if (serviceBound) {
            unbindService(serviceConnection)
            audioPlayerService.stopSelf()
        }
    }

}
