package dag.lydbok.repository

import androidx.lifecycle.MutableLiveData
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.model.setSelected
import dag.lydbok.util.Logger
import dag.lydbok.util.getDuration
import java.io.File

object Repository {
    private var isOpened = false
    lateinit var lydbøker: Lydbøker
        private set
    private lateinit var appDir: File

    lateinit var liveLydbøker: MutableLiveData<Lydbøker>
        private set

    fun open(appDir: File) {
        if (isOpened) {
            Logger.info("Repo allerede åpnet")
            return
        }
        this.appDir = appDir

        lydbøker = LydbøkerBuilder.build(appDir) { file -> file.getDuration() }
        liveLydbøker = MutableLiveData(lydbøker)
        isOpened = true
    }


    fun save() = lydbøker.forEach { ConfigStorage.save(it.lydbokDir, it.config) }

    private fun signalLydbokChanged() {
        liveLydbøker.postValue(lydbøker)
    }

    fun setLydbokCompleted() {
//        podcasts.setPlayingCompleted()
//        signalPodcastUpdate()
    }


    fun stopPlaying() {
//        signalPlayingStopped()
    }

    fun selectLydbok(lydbok: Lydbok) {
        lydbøker.setSelected(lydbok)
        signalLydbokChanged()
    }

}
