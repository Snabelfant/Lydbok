package dag.lydbok.model;

import java.io.*;
import java.util.*;

public class LydbokBuilder {

    private static String readLydbokTitle(File topDir) throws IOException {
        File titleFile = new File(topDir, "tittel.txt");
        return new BufferedReader(new FileReader(titleFile)).readLine();
    }


    public static Lydbok build(String topDirname) throws IOException {
        File topDir = new File(topDirname);
        Lydbok lydbok = new Lydbok();

        String title = readLydbokTitle(topDir);
        lydbok.setTitle(title);

        List<File> cdDirs = findTrackDirs(topDir);

        for (File cdDir : cdDirs) {
            List<Track> tracksOnCd = getTracksInDir(cdDir);
            lydbok.addTracks(tracksOnCd);
        }

        if (lydbok.getTracks().isEmpty()) {
            throw new RuntimeException("Ingen lydspor funnet i " + topDirname);
        }

        List<Track> tracks = lydbok.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setSeqNo(i);
            System.out.println(tracks.get(i));
        }

        return lydbok;
    }

    private static List<Track> getTracksInDir(File cdDir) throws IOException {
        CD cd = new CD(cdDir);

        File[] audioFiles = cdDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3") || name.endsWith(".m4a");
            }
        });

        List<Track> tracks = new ArrayList<Track>();
        for (File audioFile : audioFiles) {
            Track track = new Track(cd, audioFile);
            tracks.add(track);
        }

        Collections.sort(tracks);

        return tracks;
    }

    private static List<File> findTrackDirs(File topDir) {
        File[] cdDirs = topDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        Arrays.sort(cdDirs, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        List<File> dirs = new ArrayList<File>(Arrays.asList(cdDirs));
        dirs.add(topDir);
        return dirs;
    }
}
