package dag.lydbok.exception;

public class LydbokException extends Exception {
    public LydbokException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return "LydbokException{}" + getMessage() + "/" + getCause().toString() ;
    }
}
