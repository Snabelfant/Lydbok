package dag.lydbok.model

import java.io.File

object LydbøkerBuilder {

    fun build(appDir: File) =
        appDir
            .walkTopDown()
            .maxDepth(1)
            .filter { it.isDirectory && it != appDir }
            .map { LydbokBuilder.build(it) }.toList() as Lydbøker
}