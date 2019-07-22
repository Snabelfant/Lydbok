package dag.lydbok.repository

import androidx.lifecycle.MutableLiveData
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.model.Tracks
import dag.lydbok.model.getSelected
import dag.lydbok.util.Logger
import dag.lydbok.util.getDuration
import java.io.File

object Repository {
    private var isOpened = false
    lateinit var lydbøker: Lydbøker
    private lateinit var appDir: File
    lateinit var liveTracks: MutableLiveData<Tracks>
        private set

    lateinit var liveLydbøker: MutableLiveData<Lydbøker>
        private set

    fun open(appDir: File) {
        if (isOpened) {
            Logger.info("Repo allerede åpnet")
            return
        }
        this.appDir = appDir

        lydbøker = LydbøkerBuilder.build(appDir) { file -> file.getDuration() }
        liveTracks = MutableLiveData(lydbøker.getSelected().tracks)
        liveLydbøker = MutableLiveData(lydbøker)
        isOpened = true
    }

    private fun signalTracksUpdate() = liveTracks.postValue(lydbøker.getSelected().tracks)

    fun save() = lydbøker.forEach { ConfigStorage.save(it.lydbokDir, it.config) }

    private fun signalLydbokChanged() {
        signalTracksUpdate()
    }

    fun setSelected(lydbok: Lydbok) {
        signalLydbokChanged()
    }

    fun setLydbokCompleted() {
//        podcasts.setPlayingCompleted()
//        signalPodcastUpdate()
    }


    fun stopPlaying() {
//        signalPlayingStopped()
    }

}
