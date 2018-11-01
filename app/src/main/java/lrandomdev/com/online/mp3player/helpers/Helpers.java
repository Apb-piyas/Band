package lrandomdev.com.online.mp3player.helpers;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;

import java.io.File;
import java.util.ArrayList;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.models.MyPlaylist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.models.Track_;

/**
 * Created by Lrandom on 3/30/18.
 */

public class Helpers {
    public static void addToFavoriest(BoxStore boxStore, Track track) {
        Box<Track> box = boxStore.boxFor(Track.class);
        box.put(track);
    }

    public static ArrayList<Track> getFavoritesList(BoxStore boxStore){
        Box<Track> box = boxStore.boxFor(Track.class);
        ArrayList<Track> tracks= new ArrayList<Track>(box.getAll());
        return tracks;
    }

    public static void removeFavorites(BoxStore boxStore, String remoteId){
        Box<Track> photoBox = boxStore.boxFor(Track.class);
        Track track = photoBox.query().equal(Track_.remoteId,remoteId).build().findFirst();
        if(track!=null){
            photoBox.remove(track);
        }
    }

    public static boolean checkFavoriest(BoxStore boxStore, String remoteId) {
        Box<Track> box = boxStore.boxFor(Track.class);
        Track track = box.query().equal(Track_.remoteId,remoteId).build().findFirst();
        if(track==null){
            return false;
        }
        return true;
    }

    public static String timer(long milliseconds) {
        String finalTimer = "";
        String secondsString = "";
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimer = hours + ":";
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        finalTimer = finalTimer + minutes + ":" + secondsString;
        return finalTimer;
    }

    public static int getProgressPercentage(long currentDuration,
                                            long totalDuration) {
        Double percentage = (double) 0;
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        return percentage.intValue();
    }

    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }

    public static void shareAction(Context context, Track track) {
        String sharePath=track.getPath();
        Uri uri = Uri.parse("file:///"+sharePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("audio/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(shareIntent, "Share Sound File"));
    }

    public static void shareApp(Context context){
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }



    public static void shareAction(Context context,String url) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing");
        i.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(i, "Share"));
    }


    public static void loadAd(Context context, LinearLayout adViewWrapper){
        SharedPreferences prefs = context.getSharedPreferences("ads",Context.MODE_PRIVATE);
        String id = prefs.getString("banner_ad_unit","");
        if(!id.equalsIgnoreCase("")){
            AdRequest request = new AdRequest.Builder()
                    .addTestDevice("C977CB1F462AC57AFD234CD2869D9DD0")
                    .build();
            NativeExpressAdView adView = new NativeExpressAdView(context);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(id);
            if(adView.getAdSize() != null && adView.getAdUnitId() != null) {
                adView.loadAd(request);
            }

            adViewWrapper.addView(adView);
        }
    }

    public static void loadInAd(Context context){
        SharedPreferences prefs = context.getSharedPreferences("ads",Context.MODE_PRIVATE);
        String id = prefs.getString("in_ad_unit","");
        if(!id.equalsIgnoreCase("")) {
            AdRequest request = new AdRequest.Builder()
                    .addTestDevice("C977CB1F462AC57AFD234CD2869D9DD0")
                    .build();
            final InterstitialAd mInterstitialAd = new InterstitialAd(context);
            mInterstitialAd.setAdUnitId(id);
            mInterstitialAd.loadAd(request);
            mInterstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mInterstitialAd.show();
                }
            });
        }
    }

    public static Bitmap decodeFile(String photoPath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferQualityOverSpeed = true;

        return BitmapFactory.decodeFile(photoPath, options);
    }


    public static void removeTrackInPlaylist(BoxStore boxStore, Track track) {
        Box<Track> box = boxStore.boxFor(Track.class);
        box.remove(track);
    }

    public static void removePlaylist(BoxStore boxStore, Long id){
        Box<Track> boxTrack = boxStore.boxFor(Track.class);
        ArrayList<Track> tracks= new ArrayList<Track>(boxTrack.query().equal(Track_.playlistId,id).build().find());
        boxTrack.remove(tracks);

        Box<MyPlaylist> box = boxStore.boxFor(MyPlaylist.class);
        box.remove(id);
    }

    public static String trimRightComma(String text){
        if (text.endsWith(" - ")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static Track getTrackFromAbPath(Context context,File file){
        ContentResolver contentResolver = context.getContentResolver();
        try {
            String path = file.getCanonicalPath();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[] { MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DURATION };
            Cursor cursor = contentResolver
                    .query(uri, projection, MediaStore.Audio.Media.DATA+" = ?", new String[]{path}, null);
            ArrayList<Track> tracks= convertMediaStoreCursorToArrayList(cursor);
            if(tracks.size()!=0){
                Track track = tracks.get(0);
                track.setRealPath(file.getAbsolutePath());
                return track;
            }else{
                Track track = new Track();
                track.setPath(file.getPath());
                track.setTitle(file.getName());
                track.setRealPath(file.getAbsolutePath());
                return  track;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Track> convertMediaStoreCursorToArrayList(Cursor cursor) {
        ArrayList<Track> trackList = new ArrayList<Track>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Track track = new Track();
                track.setId((long)cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media._ID)));
                track.setArtist(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                track.setTitle(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE)));
                track.setPath(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA)));
                track.setAlbumId(Integer.parseInt(cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))));
                track.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                track.setRemoteId(null);
                trackList.add(track);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return trackList;
    }

    public static void openWebsite(Context context,String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }


}
