package dag.lydbok.repository

import androidx.lifecycle.MutableLiveData
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.model.selected
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
        if (!isOpened) {
            this.appDir = appDir
            lydbøker = LydbøkerBuilder.build(appDir) { file -> file.getDuration() }
            liveLydbøker = MutableLiveData(lydbøker)
            isOpened = true
        }
    }


    fun saveAll() = lydbøker.forEach { save(it) }
    fun saveSelected() = save(lydbøker.selected)

    private fun save(lydbok: Lydbok) = ConfigStorage.save(lydbok.lydbokDir, lydbok.config)

    private fun signalLydbokChanged() {
        liveLydbøker.postValue(lydbøker)
    }

    fun selectLydbok(lydbok: Lydbok) {
        lydbøker.selected = lydbok
        signalLydbokChanged()
    }

    fun setPlaying(tracName: String) {
        with(lydbøker.selected) {
            setCurrentTrack(tracName)
            save(this)
        }
        signalLydbokChanged()
    }

    fun updateTrackPosition(currentTrackOffset: Int) {
        lydbøker.selected.currentTrackOffset = currentTrackOffset
    }

}

