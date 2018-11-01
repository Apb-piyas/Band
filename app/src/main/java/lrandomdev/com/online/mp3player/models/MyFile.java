package lrandomdev.com.online.mp3player.models;

import android.support.annotation.NonNull;

import java.io.File;
import java.net.URI;

/**
 * Created by Lrandom on 4/11/18.
 */

public class MyFile extends File{
    Boolean isTrack=false;

    public MyFile(@NonNull String pathname) {
        super(pathname);
    }

    public MyFile(String parent, @NonNull String child) {
        super(parent, child);
    }

    public MyFile(File parent, @NonNull String child) {
        super(parent, child);
    }

    public MyFile(@NonNull URI uri) {
        super(uri);
    }

    public Boolean getIsTrack() {
        return isTrack;
    }

    public MyFile setIsTrack(Boolean track) {
        isTrack = track;
        return this;
    }
}
