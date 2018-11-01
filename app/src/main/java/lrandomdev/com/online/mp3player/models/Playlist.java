package lrandomdev.com.online.mp3player.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lrandomdev.com.online.mp3player.helpers.RestClient;

/**
 * Created by Lrandom on 3/29/18.
 */

public class Playlist implements Serializable{
    @Expose
    @SerializedName("id")
    long id;

    @Expose
    @SerializedName("name")
    private String name;

    @SerializedName("number_of_track")
    private int total_track;

    @Expose
    @SerializedName("image")
    private String thumb;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Playlist setName(String name) {
        this.name = name;
        return this;
    }

    public int getTotal_track() {
        return total_track;
    }

    public Playlist setTotal_track(int total_track) {
        this.total_track = total_track;
        return this;
    }

    public String getThumb() {

        return RestClient.BASE_URL+thumb;
    }

    public Playlist setThumb(String thumb) {
        this.thumb = thumb;
        return this;
    }

}
