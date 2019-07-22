package dag.lydbok.repository

import dag.lydbok.model.Lydbøker
import dag.lydbok.util.AudioFileDuration
import java.io.File

object LydbøkerBuilder {

    fun build(appDir: File, duration: AudioFileDuration) =
        appDir
            .walkTopDown()
            .maxDepth(1)
            .filter { it.isDirectory && it != appDir }
            .map { LydbokBuilder.build(it, duration) }
            .toList()
            .also { ensureSingleSelected(it) }

    private fun ensureSingleSelected(lydbøker: Lydbøker) {
        if ((lydbøker.count { it.isSelected } != 1)) {
            lydbøker
                .onEach { it.isSelected = false }
                .also { lydbøker.first().isSelected = true }
        }
    }
}