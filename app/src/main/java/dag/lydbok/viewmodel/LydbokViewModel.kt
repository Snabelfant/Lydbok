package dag.lydbok.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dag.lydbok.audioplayer.AudioPlayerCommands
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.repository.Repository
import java.io.IOException

class LydbokViewModel @Throws(IOException::class)
constructor(application: Application) : AndroidViewModel(application) {
    val liveLydbøker: LiveData<Lydbøker>
    private val audioPlayerCommands = AudioPlayerCommands(application.applicationContext)

    private val newTrackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newTrackName = intent.getStringExtra("trackfile")
            Repository.setPlaying(newTrackName)
        }
    }

    init {
        Repository.open(application.getExternalFilesDir(null)!!)
        liveLydbøker = Repository.liveLydbøker
        audioPlayerCommands.registerNewTrackReceiver(newTrackReceiver)
    }

    fun saveAll() {
        Repository.saveAll()
    }

    fun selectLydbok(lydbok: Lydbok) {
        Repository.selectLydbok(lydbok)
        audioPlayerCommands.setTrackFiles(lydbok.trackFiles, lydbok.currentTrackFile, lydbok.currentTrackOffset)
    }

    fun updateTrackPosition(currentPosition: Int) {
        Repository.updateTrackPosition(currentPosition)
    }

    fun saveSelected() {
        Repository.saveSelected()
    }

    fun playNext() {
        Repository.playNext()
    }
}
