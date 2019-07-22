package dag.lydbok.model

class LydbokException : Exception {
    constructor(message: String, cause: Throwable) : super(message, cause) {}

    constructor(message: String) : super(message) {}

    override fun toString(): String {
        return "LydbokException{}$message/${cause.toString()}"
    }
}
