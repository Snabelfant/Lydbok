package dag.lydbok.model;

import java.io.File;

public class CD {
    private File dir;

    public CD(File cdDir) {
        this.dir = cdDir;
    }

    public String getName() {
        return dir.getName();
    }
}
