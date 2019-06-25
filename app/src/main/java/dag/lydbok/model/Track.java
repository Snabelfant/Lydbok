package dag.lydbok.model;

import dag.lydbok.exception.LydbokException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;

public class Track implements Comparable {
    private String title;
    private CD cd;
    private File audioFile;
    private int durationInSecs;
    private String sortName;
    private Exception exception;
    private int seqNo;

    @Override
    public String toString() {
        return "Track{" +
                "#=" + seqNo +
                ", title='" + title + '\'' +
                ", cd=" + cd.getName() +
                ", audioFile=" + audioFile +
                ", durationInSecs=" + durationInSecs +
                ", sortName='" + sortName + '\'' +
                ", exception=" + exception +
                '}';
    }

    public Track(CD cd, File audioFile) {
        this.cd = cd;
        this.audioFile = audioFile;
        this.title = audioFile.getName();
        sortName = cd.getName() + "-" + audioFile.getName();
        exception = null;
        try {
            readTags();
        } catch (LydbokException e) {
            exception = e;
        }
    }

    public String getTitle() {
        return title;
    }

    public boolean isCorrupt() {
        return exception != null;
    }

    public int getDurationInSecs() {
        return durationInSecs;
    }

    private void readTags() throws LydbokException {

        AudioFile f = null;
        try {
            f = AudioFileIO.read(audioFile);
        } catch (CannotReadException e) {
            readTagsException(e);
        } catch (IOException e) {
            readTagsException(e);
        } catch (org.jaudiotagger.tag.TagException e) {
            readTagsException(e);
        } catch (ReadOnlyFileException e) {
            readTagsException(e);
        } catch (InvalidAudioFrameException e) {
            readTagsException(e);
        }
        Tag tag = f.getTag();
        String title = tag.getFirst(FieldKey.TITLE);

        if (title != null && !"".equals(title)) {
            this.title = title;
        }

        AudioHeader header = f.getAudioHeader();
        durationInSecs = header.getTrackLength();
    }

    private void readTagsException(Exception e) throws LydbokException {
        throw new LydbokException(sortName, e);
    }

    public int compareTo(Object o) {
        Track that = (Track) o;

        return sortName.compareTo(that.sortName);
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }
}
