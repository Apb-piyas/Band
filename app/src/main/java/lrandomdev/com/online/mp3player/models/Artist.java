package lrandomdev.com.online.mp3player.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lrandomdev.com.online.mp3player.helpers.RestClient;

/**
 * Created by Lrandom on 3/29/18.
 */

public class Artist implements Serializable {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    @Expose
    private String artist;

    @SerializedName("image")
    private String thumb;


    public int getId() {
        return id;
    }

    public Artist setId(int id) {
        this.id = id;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public Artist setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getThumb() {
        return RestClient.BASE_URL+thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
