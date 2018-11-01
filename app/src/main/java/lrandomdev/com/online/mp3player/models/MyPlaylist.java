package lrandomdev.com.online.mp3player.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class MyPlaylist{
    @Id(assignable = true)
    long id;

    private String name;
    private int total_track;
    private String thumb;
    private Boolean checked=false;
    private Boolean selected = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal_track() {
        return total_track;
    }

    public void setTotal_track(int total_track) {
        this.total_track = total_track;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
