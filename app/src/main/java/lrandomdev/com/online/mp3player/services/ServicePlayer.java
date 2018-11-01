package lrandomdev.com.online.mp3player.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;


import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Random;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;

/**
 * Created by Lrandom on 3/30/18.
 */
public class ServicePlayer extends Service implements
        MediaPlayer.OnCompletionListener,MediaPlayer.OnBufferingUpdateListener,MediaPlayer.OnErrorListener,MediaPlayer.OnPreparedListener{
    MediaPlayer mediaPlayer;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    boolean isRepeat, isShuffle, isBuffering = false,isPlayAfterBuffering = true;
    int trackIndex=-1;
    ArrayList<Track> tracks;
    private int buffer;
    public static String PAUSE = "lrandomdev.com.online.mp3player.PAUSE";
    public static String PLAY = "lrandomdev.com.online.mp3player.PLAY";
    public static String NEXT = "lrandomdev.com.online.mp3player.NEXT";
    public static String PREV = "lrandomdev.com.online.mp3player.PREV";
    public static String CLOSE = "lrandomdev.com.online.mp3player.CLOSE";
    public static String ALARM_PAUSE= "lrandomdev.com.online.mp3player.ALARM_PAUSE";
    private final IBinder mBinder = new PlayerBinder();
    private int NOTIFICATION_ID = 111;

    public static final String BUFFERING = "lrandomdev.com.online.mp3player.ServicePlayer.BUFFERING";
    public static final String UPDATE_UI = "lrandomdev.com.online.mp3player.ServicePlayer.UPDATE_UI";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state != null) {
                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    // Call Dropped or rejected or Restart service play media
                    resume();
                } else {
                    pause();
                }
            }

            String action = intent.getAction();
            if (action != null) {

                if (action.equals(PAUSE) || action.equals(ALARM_PAUSE)) {
                    pause();
                }

                if (action.equals(PLAY)) {
                    resume();
                }

                if (action.equals(NEXT)) {
                    tracks.get(trackIndex).setSelected(false);
                    if (!isShuffle) {
                        if (trackIndex == (tracks.size() - 1)) {
                            trackIndex = 0;
                        } else {
                            trackIndex += 1;
                        }
                    } else {
                        Random rand = new Random();
                        trackIndex = rand.nextInt((tracks.size() - 1) - 0 + 1) + 0;
                    }
                    play(trackIndex, tracks);
                }

                if (action.equals(PREV)) {
                    tracks.get(trackIndex).setSelected(false);
                    if (isShuffle) {
                        if (trackIndex == 0) {
                            trackIndex = tracks.size() - 1;
                        } else {
                            trackIndex -= 1;
                        }
                    } else {
                        Random rand = new Random();
                        trackIndex = rand.nextInt((tracks.size() - 1) - 0 + 1) + 0;
                    }
                    play(trackIndex, tracks);
                }

                if(action.equals(CLOSE)){
                    pause();
                    hideNotification();
                }
            }
        }
    };


    public void showNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "controls_channel_id";
            String CHANNEL_NAME = "controls_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null,null);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                        .setChannelId(CHANNEL_ID)
                        .setContentText("s")
                        .setContentTitle("s");
        }else{
            notificationBuilder = new NotificationCompat.Builder(
                    getApplicationContext());
        }

        Track track = tracks.get(trackIndex);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setTextViewText(R.id.tvTitle, track.getTitle());
        RemoteViews remoteSmallViews= new RemoteViews(getPackageName(),R.layout.layout_notification_small);
        remoteSmallViews.setTextViewText(R.id.tvTitle, track.getTitle());

        String artist_text = "";
        ArrayList<Artist> artists = track.getArtists();

        if(artists!=null &&  artists.size()!=0) {
            for (int i = 0; i < artists.size(); i++) {
                if(i == (artists.size()-1)){
                    artist_text += artists.get(i).getArtist();
                }else {
                    artist_text += artists.get(i).getArtist() + " , ";
                }
            }
            remoteSmallViews.setTextViewText(R.id.tvArtist, artist_text);
            remoteViews.setTextViewText(R.id.tvArtist, artist_text);
        }else {
            remoteSmallViews.setTextViewText(R.id.tvArtist, track.getArtist());
            remoteViews.setTextViewText(R.id.tvArtist, track.getArtist());
        }

        Intent intent = new Intent(this, ActivityHome.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        if (isPlay()) {
            Intent pauseIntent = new Intent(PAUSE);
            PendingIntent pendingPauseIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    0, pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.btnPlay, pendingPauseIntent);
            remoteViews.setImageViewResource(R.id.btnPlay, R.drawable.ic_stop_no_circle);

            remoteSmallViews.setOnClickPendingIntent(R.id.btnPlay, pendingPauseIntent);
            remoteSmallViews.setImageViewResource(R.id.btnPlay, R.drawable.ic_stop_no_circle);
        } else {
            Intent playIntent = new Intent(PLAY);
            PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    0, playIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.btnPlay, pendingPlayIntent);
            remoteViews.setImageViewResource(R.id.btnPlay, R.drawable.ic_play_no_circle);

            remoteSmallViews.setOnClickPendingIntent(R.id.btnPlay, pendingPlayIntent);
            remoteSmallViews.setImageViewResource(R.id.btnPlay, R.drawable.ic_play_no_circle);
        }

        Intent nextIntent = new Intent(NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btnNext, pendingNextIntent);
        remoteSmallViews.setOnClickPendingIntent(R.id.btnNext, pendingNextIntent);

        Intent prevIntent = new Intent(PREV);
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btnPrev, pendingPrevIntent);

        remoteSmallViews.setOnClickPendingIntent(R.id.btnPrev, pendingPrevIntent);

        Intent closeIntent = new Intent(CLOSE);
        PendingIntent pendingCloseIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, closeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btnClose, pendingCloseIntent);
        remoteSmallViews.setOnClickPendingIntent(R.id.btnClose, pendingCloseIntent);

        notificationBuilder
                .setSmallIcon(R.drawable.ic_play_no_circle)
                .setContentIntent(contentIntent)
                .setCustomContentView(remoteSmallViews)
                .setCustomBigContentView(remoteViews);

        Notification notification = notificationBuilder.build();
        startForeground(NOTIFICATION_ID,notification);
        NotificationTarget notificationTarget = new NotificationTarget(
                getApplicationContext()
                ,R.id.imgThumb,remoteViews
                ,notification,NOTIFICATION_ID);

        if(track.getThumb()!=null) {
            String thumb = track.getThumb();
            if(thumb!=null){
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(RestClient.BASE_URL+tracks.get(trackIndex).getThumb())
                        .into(notificationTarget);
            }else{
                remoteViews.setImageViewResource(R.id.imgThumb,R.drawable.bg);
            }
        }else{
            remoteViews.setImageViewResource(R.id.imgThumb,R.drawable.bg);
        }
    }



    public void hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_REDELIVER_INTENT;
    }

    public class PlayerBinder extends Binder {
        public ServicePlayer getService() {
            return ServicePlayer.this;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    public void onCreate() {
        super.onCreate();
        // instance player object
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnErrorListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(PAUSE);
        filter.addAction(PLAY);
        filter.addAction(NEXT);
        filter.addAction(PREV);
        filter.addAction(CLOSE);
        filter.addAction(ALARM_PAUSE);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        } catch (Exception e) {
            // TODO: handle exception
        }
        Log.i("DESTROY SERVICE", "destroy");
        unregisterReceiver(receiver);
    }

    public void play(int trackIndex, ArrayList<Track> tracks) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                this.tracks = tracks;
                this.trackIndex = trackIndex;
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this.tracks.get(trackIndex).getPath());
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();
                isBuffering = true;
                isPlayAfterBuffering = true;
                Intent intent = new Intent(BUFFERING);
                ServicePlayer.this.sendBroadcast(intent);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Log.e("LOI ME ROI","LOI CMNR");
        }
    };

    public void pause() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Intent intent = new Intent(UPDATE_UI);
                ServicePlayer.this.sendBroadcast(intent);
                showNotification();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void resume() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                Intent intent = new Intent(UPDATE_UI);
                ServicePlayer.this.sendBroadcast(intent);
                showNotification();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public int getTotalTime() {
        if (isBuffering == false) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getElapsedTime() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean getBuffering() {
        return this.isBuffering;
    }

    public void setBuffering(boolean flag) {
        this.isBuffering = flag;
    }

    public boolean getPlayAfterBuffering() {
        return this.isPlayAfterBuffering;
    }

    public void setPlayAfterBuffering(boolean flag) {
        if (flag == false) {
            cancelNotification();
        } else {
            showNotification();
        }
        this.isPlayAfterBuffering = flag;
    }

    public void cancelNotification() {
        stopForeground(true);
    }


    public void seek(int pos) {
        mediaPlayer.seekTo(pos);
    }

    public boolean isPlay() {
        if (mediaPlayer.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }


    public void setRepeat(boolean flag) {
        this.isRepeat = flag;
    }

    public void setShuffle(boolean flag) {
        this.isShuffle = flag;
    }

    public boolean getRepeat() {
        return this.isRepeat;
    }

    public boolean getShuffle() {
        return this.isShuffle;
    }

    public ArrayList<Track> getTracks() {
        return this.tracks;
    }

    public void addTrackToQueue(Track track){
        if(this.tracks!=null) {
            this.tracks.add(track);
        }
    }

    public Track getTrack(){
        return this.tracks.get(trackIndex);
    }

    public int getTrackIndex() {
        return this.trackIndex;
    }



    public void onCompletion(MediaPlayer mp) {
        if (!isBuffering) {
            // TODO Auto-generated method stub
            tracks.get(trackIndex).setSelected(false);
            if (!this.isRepeat) {
                if (!this.isShuffle) {
                    if (this.trackIndex == (this.tracks.size() - 1)) {
                        this.trackIndex = 0;
                    } else {
                        this.trackIndex += 1;
                    }
                } else {
                    Random rand = new Random();
                    this.trackIndex = rand
                            .nextInt((this.tracks.size() - 1) - 0 + 1) + 0;
                }
            }
            tracks.get(trackIndex).setSelected(true);
            play(trackIndex, tracks);
        }
    }

    public int getAudioSessionId(){
        return mediaPlayer.getAudioSessionId();
    }

    public void setAuxEffectSendLevel(float level){
        mediaPlayer.setAuxEffectSendLevel(level);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        this.buffer = i;
    }

    public int getBufferingDownload() {
        return this.buffer;
    }


    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        // TODO Auto-generated method stub
        if (tracks != null && trackIndex == tracks.size()) {
            trackIndex = 0;
        } else {
            trackIndex++;
        }
        play(trackIndex, tracks);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (isPlayAfterBuffering) {
            mediaPlayer.start();
            showNotification();
            Intent intent = new Intent(UPDATE_UI);
            ServicePlayer.this.sendBroadcast(intent);
        }
        isBuffering=false;
    }
}