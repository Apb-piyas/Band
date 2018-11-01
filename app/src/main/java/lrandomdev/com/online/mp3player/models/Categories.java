package lrandomdev.com.online.mp3player.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import lrandomdev.com.online.mp3player.helpers.RestClient;

public class Categories implements Serializable {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("name")
    private String title;

    @SerializedName("image")
    private String thumb;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    public int getId() {
        return id;
    }

    public Categories setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Categories setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getThumb() {
        return RestClient.BASE_URL+thumb;
    }

    public Categories setThumb(String thumb) {
        this.thumb = thumb;
        return this;
    }
}
