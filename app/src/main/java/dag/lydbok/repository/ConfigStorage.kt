package dag.lydbok.repository

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import dag.lydbok.model.Config
import dag.lydbok.model.Position
import dag.lydbok.model.Tracks
import dag.lydbok.util.JsonMapper
import dag.lydbok.util.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ConfigStorage {

    fun save(lydbokDir: File, config: Config) {
        val configFile = File(lydbokDir, "konfig.json")
        Logger.info("Konfig skrives til ${configFile.absolutePath}")
        val savedConfig =
            SavedConfig(config.isSelected, config.currentPosition.track.title, config.currentPosition.offset)
        JsonMapper().write(FileOutputStream(configFile), savedConfig, true)
    }

    fun load(lydbokDir: File, tracks: Tracks): Config {
        val configFile = File(lydbokDir, "konfig.json")
        return if (configFile.exists()) {
            Logger.info("Konfig leses fra ${configFile.absolutePath}")
            val savedConfig = JsonMapper().read(FileInputStream(configFile), object : TypeReference<SavedConfig>() {})
            Config(
                savedConfig.isSelected,
                Position(
                    tracks.find { it.title == savedConfig.currentTrack }!!,
                    savedConfig.offset
                )
            )
        } else {
            Logger.info("Ny konfig for ${lydbokDir.absolutePath}")
            Config(false, Position(tracks[0], 0)).also { save(lydbokDir, it) }
        }
    }

    private class SavedConfig @JsonCreator constructor(
        @JsonProperty("selected") val isSelected: Boolean,
        @JsonProperty("currentTrack") val currentTrack: String,
        @JsonProperty("offset") val offset: Int
    )
}
