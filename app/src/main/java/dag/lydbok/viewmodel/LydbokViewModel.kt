package dag.lydbok.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import dag.lydbok.model.Lydbøker
import dag.lydbok.model.Track
import dag.lydbok.model.Tracks
import dag.lydbok.repository.Repository
import java.io.IOException

class LydbokViewModel @Throws(IOException::class)
constructor(application: Application) : AndroidViewModel(application) {
    val liveLydbøker: LiveData<Lydbøker>

    val liveTracks: LiveData<Tracks>

    init {
        Repository.open(application.getExternalFilesDir(null)!!)
        liveLydbøker = Repository.liveLydbøker
        liveTracks = Repository.liveTracks
    }

    fun setPlaying(track: Track) {
//        Repository.setPlaying(podcast)
    }

    fun setPlayingCompleted() {
//        Repository.setPlayingCompleted()
    }

    fun stopPlaying() {
        Repository.stopPlaying()
    }

    fun save() {
        Repository.save()
    }
}
