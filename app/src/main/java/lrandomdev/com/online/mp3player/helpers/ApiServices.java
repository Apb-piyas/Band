package lrandomdev.com.online.mp3player.helpers;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.models.Album;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Categories;
import lrandomdev.com.online.mp3player.models.Playlist;
import lrandomdev.com.online.mp3player.models.Track;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Lrandom on 26/05/2017.
 */

public interface ApiServices {
    @GET("audios_api/audios")
    Call<ArrayList<Track>> getTracks(
            @Query("first") int first,
            @Query("offset") int offset,
            @Query("q") String query
    );

    @GET("audios_api/audios")
    Call<ArrayList<Track>> getTracks(
            @Query("first") int first,
            @Query("offset") int offset,
            @Query("categories_id") String categories_id,
            @Query("album_id") String album_id,
            @Query("artist_id") String artist_id,
            @Query("playlist_id") String playlist_id
    );

    @GET("audios_api/audio")
    Call<ArrayList<Track>> getTracksById(
            @Query("track_id") String trackId
    );

    @GET("categories_api/categories")
    Call<ArrayList<Categories>> getCategories(
            @Query("first") int first,
            @Query("offset") int offset,
            @Query("q") String query
    );

    @GET("albums_api/albums")
    Call<ArrayList<Album>> getAlbums(
            @Query("first") int first,
            @Query("offset") int offset,
            @Query("q") String query
    );

    @GET("artists_api/artists")
    Call<ArrayList<Artist>> getArtists(
            @Query("first") int first,
            @Query("offset") int offset,
            @Query("q") String query
    );

    @GET("playlists_api/playlists")
    Call<ArrayList<Playlist>> getPlaylists(
            @Query("first") int first,
            @Query("offset") int offset,
            @Query("q") String query
    );

    @GET("settings_api/general")
    Call<JsonObject> getGeneralSetting();

    @GET("settings_api/ads")
    Call<JsonObject> getAds();

    @GET
    Call<JsonObject> getLyrics(@Url String url,@Query("format") String format ,@Query("artist") String artist,@Query("title") String title);

    @GET("audios_api/audios")
    Call<ArrayList<Track>> pullTrack(@Query("pull") String id);

    @GET("albums_api/albums")
    Call<ArrayList<Album>> pullAlbum(@Query("pull") String id);

    @GET("artists_api/artists")
    Call<ArrayList<Artist>> pullArtist(@Query("pull") String id);

    @GET("artists_api/categories")
    Call<ArrayList<Categories>> pullCategories(@Query("pull") String id);


    @GET("playlists_api/playlists")
    Call<ArrayList<Playlist>> pullPlaylists(@Query("pull") String id);

    @GET()
    @Streaming
    Call<ResponseBody> downloadAudio(@Url String url);

}
