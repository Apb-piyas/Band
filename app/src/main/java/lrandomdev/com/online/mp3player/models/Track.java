package lrandomdev.com.online.mp3player.models;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

/**
 * Created by Lrandom on 3/29/18.
 */

@Entity
public class Track  implements Serializable{
    @Id(assignable = true)
    long id;

    @Expose
    @SerializedName("remoteId")
    private String remoteId=null;

    @Expose
    @SerializedName("name")
    private  String title;

    @Expose
    @SerializedName("artist_id")
    private  String artist;

    @Expose
    @SerializedName("path")
    private  String path;

    @Expose
    @SerializedName("artists")
    @Convert(converter = ArtistConverter.class,dbType = String.class)
    private ArrayList<Artist> artists;

    @Expose
    @SerializedName("thumb")
    private String thumb;

    @SerializedName("description")
    private String description;


    private String realPath;

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    private long playlistId;

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    private  String size;
    private  String duration;
    private  String extension;
    private int albumId;
    private Boolean selected = false;
    private Boolean checked = false;

    private boolean favoriest;


    public boolean isFavoriest() {
        return favoriest;
    }

    public void setFavoriest(boolean favoriest) {
        this.favoriest = favoriest;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
    }

    public Boolean getSelected() {
        return selected;
    }

    public Track setSelected(Boolean selected) {
        this.selected = selected;
        return this;
    }

    public Boolean getChecked() {
        return checked;
    }

    public Track setChecked(Boolean checked) {
        this.checked = checked;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Track setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public Track setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Track setPath(String path) {
        this.path = path;
        return this;
    }

    public String getSize() {
        return size;
    }

    public Track setSize(String size) {
        this.size = size;
        return this;
    }

    public String getDuration() {
        return duration;
    }

    public Track setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    public String getExtension() {
        return extension;
    }

    public Track setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    public int getAlbumId() {
        return albumId;
    }

    public Track setAlbumId(int albumId) {
        this.albumId = albumId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class ArtistConverter implements PropertyConverter<ArrayList<Artist>,String>{
        @Override
        public ArrayList<Artist> convertToEntityProperty(String databaseValue) {
            Gson gson = new Gson();
            Type type= new TypeToken<ArrayList<Artist>>(){}.getType();
            ArrayList<Artist> artists = gson.fromJson(databaseValue,type);
            return artists;
        }

        @Override
        public String convertToDatabaseValue(ArrayList<Artist> entityProperty) {
            Gson gson = new Gson();
            return gson.toJson(entityProperty);
        }
    }
}
