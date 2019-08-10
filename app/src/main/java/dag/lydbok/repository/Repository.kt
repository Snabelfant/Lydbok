package dag.lydbok.repository

import androidx.lifecycle.MutableLiveData
import dag.lydbok.model.Lydbok
import dag.lydbok.model.Lydbøker
import dag.lydbok.model.getSelected
import dag.lydbok.model.setSelected
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
    fun saveSelected() = save(lydbøker.getSelected())

    private fun save(lydbok: Lydbok) = ConfigStorage.save(lydbok.lydbokDir, lydbok.config)

    private fun signalLydbokChanged() {
        liveLydbøker.postValue(lydbøker)
    }

    fun selectLydbok(lydbok: Lydbok) {
        lydbøker.setSelected(lydbok)
        signalLydbokChanged()
    }

    fun setPlaying(tracName: String) {
        with(lydbøker.getSelected()) {
            setCurrentTrack(tracName)
            save(this)
        }
        signalLydbokChanged()
    }

    fun updateTrackPosition(currentTrackOffset: Int) {
        lydbøker.getSelected().currentTrackOffset = currentTrackOffset
    }

    fun playNext() {
        lydbøker.getSelected().nextTrack()
        signalLydbokChanged()
    }
}

