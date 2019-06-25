package dag.lydbok.model;

import java.util.ArrayList;
import java.util.List;

public class PlayInfo {
    private Track currentTrack;
    private int positionInCurrentTrack;
    private List<PlayHistory> playHistory;


    public PlayInfo() {
        currentTrack = null;
        positionInCurrentTrack = 0;
        playHistory = new ArrayList<PlayHistory>();
    }
}
