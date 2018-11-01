package lrandomdev.com.online.mp3player.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import lrandomdev.com.online.mp3player.helpers.RestClient;

/**
 * Created by Lrandom on 3/28/18.
 */

public class Album implements Serializable{
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    private String title;

    @SerializedName("image")
    private String thumb;

    @SerializedName("artists")
    private ArrayList<Artist> artists;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    public int getId() {
        return id;
    }

    public Album setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Album setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getThumb() {
        return RestClient.BASE_URL+thumb;
    }

    public Album setThumb(String thumb) {
        this.thumb = thumb;
        return this;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
    }
}
