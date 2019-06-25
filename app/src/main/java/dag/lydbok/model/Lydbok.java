package dag.lydbok.model;

import java.util.ArrayList;
import java.util.List;

public class Lydbok {
    private String title;
    private List<Track> tracks;
    private List<Track> corruptedTracks;
    private PlayInfo playInfo;
    private int totalDurationInSecs;

    public Lydbok() {
        tracks = new ArrayList<Track>();
        corruptedTracks = new ArrayList<Track>();
        playInfo = new PlayInfo();
        totalDurationInSecs = 0;
    }

    public List<Track> getCorruptedTracks() {
        return corruptedTracks;
    }

    public int getTotalTracks() {
        return tracks.size();
    }

    public int getTotalDurationInSecs() {
        return totalDurationInSecs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void addTracks(List<Track> newTracks) {
        for (Track newTrack : newTracks) {
            tracks.add(newTrack);
            totalDurationInSecs += newTrack.getDurationInSecs();
            if (newTrack.isCorrupt()) {
                corruptedTracks.add(newTrack);
            }
        }
    }


    public PlayInfo getPlayInfo() {
        return playInfo;
    }

    public void setPlayInfo(PlayInfo playInfo) {
        this.playInfo = playInfo;
    }
}
