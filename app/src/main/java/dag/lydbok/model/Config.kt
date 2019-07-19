package dag.lydbok.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import dag.lydbok.util.JsonMapper
import dag.lydbok.util.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Config(var currentPosition: Position) {

    fun save(lydbokDir: File) {
        val configFile = File(lydbokDir, "konfig.json")
        Logger.info("Konfig skrives til ${configFile.absolutePath}")
        val savedConfig = SavedConfig(currentPosition.track.title, currentPosition.offset)
        JsonMapper.write(FileOutputStream(configFile), savedConfig, true)
    }

    companion object {
        fun load(lydbokDir: File, tracks: Tracks): Config {
            val configFile = File(lydbokDir, "konfig.json")
            return if (configFile.exists()) {
                Logger.info("Konfig leses fra ${configFile.absolutePath}")
                val savedConfig = JsonMapper.read(FileInputStream(configFile), object : TypeReference<SavedConfig>() {})
                Config(Position(tracks.find { it.title == savedConfig.currentTrack }!!, savedConfig.offset))
            } else {
                Logger.info("Ny konfig for ${lydbokDir.absolutePath}")
                Config(Position(tracks[0], 0))
            }

        }

    }

    private class SavedConfig @JsonCreator constructor(
        @JsonProperty("currentTrack") val currentTrack: String, @JsonProperty(
            "offset"
        ) val offset: Int
    )
}
