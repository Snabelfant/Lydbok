package dag.lydbok.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dag.lydbok.audioplayer.AudioPlayerCommands
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.model.Track
import dag.lydbok.repository.Repository
import java.io.IOException

class LydbokViewModel @Throws(IOException::class)
constructor(application: Application) : AndroidViewModel(application) {
    val liveLydbøker: LiveData<Lydbøker>
    private val audioPlayerCommands = AudioPlayerCommands(application.applicationContext)

    init {
        Repository.open(application.getExternalFilesDir(null)!!)
        liveLydbøker = Repository.liveLydbøker
    }

    fun setPlaying(track: Track) {
        Repository.setPlaying(track)
    }

    fun saveAll() {
        Repository.saveAll()
    }

    fun selectLydbok(lydbok: Lydbok) {
        Repository.selectLydbok(lydbok)
        audioPlayerCommands.setTrackFiles(lydbok.trackFiles, lydbok.currentTrackFile)
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
