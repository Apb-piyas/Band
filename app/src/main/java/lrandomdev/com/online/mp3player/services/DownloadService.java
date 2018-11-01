package lrandomdev.com.online.mp3player.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityHome;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Lrandom on 5/26/18.
 */

public class DownloadService extends IntentService {
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    ApiServices apiServices;
    private int totalFileSize;
    String urlDownload;
    String fileName;
    String extension;
    Track track;

    public static final String DOWNLOAD_COMPLETED="com.lrandom.niem.download_complted";

    String fullFilePath=null;
    public DownloadService() {
        super("Download Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        track = (Track) intent.getSerializableExtra("file");
        this.urlDownload=track.getPath();
        this.fileName=track.getTitle();
        this.extension=urlDownload.substring(urlDownload.lastIndexOf(".") + 1, urlDownload.length());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "download_channel_id";
            String CHANNEL_NAME = "download_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null,null);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setChannelId(CHANNEL_ID)
                    .setContentText(this.fileName)
                    .setContentTitle(getString(R.string.download)).setSmallIcon(R.drawable.ic_download).setAutoCancel(true);
        }else{
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_download)
                    .setContentTitle(getString(R.string.download))
                    .setContentText(this.fileName)
                    .setAutoCancel(true);
        }

        notificationManager.notify(0, notificationBuilder.build());
        initDownload();
    }

    private void initDownload() {
        apiServices = RestClient.getApiService();
        Call<ResponseBody> request = apiServices.downloadAudio(this.urlDownload);
        try {
            downloadFile(request.execute().body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(ResponseBody body) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ File.separator + getString(R.string.app_name).replace(" ","");
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        File outputFile = new File(dir, this.fileName+"."+this.extension);
        this.fullFilePath=outputFile.getAbsolutePath();
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;

        while ((count = bis.read(data)) != -1) {
            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));
            int progress = (int) ((total * 100) / fileSize);
            long currentTime = System.currentTimeMillis() - startTime;

            if (currentTime > 1000 * timeCount) {
                sendNotification(progress, current);
                timeCount++;
            }
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        bis.close();

        onDownloadComplete();
    }

    private void sendNotification(int progress,double current) {
            notificationBuilder.setProgress(100, progress, false);
            Intent intent = new Intent(getBaseContext(), ActivityHome.class);
            intent.putExtra("DOWNLOAD","DOWNLOAD");
            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),100,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder
                    .setContentText(getString(R.string.downloading_file) +" "+ this.fileName + current + "/" + totalFileSize + " MB")
                    .setContentIntent(pendingIntent);
            notificationManager.notify(0, notificationBuilder.build());
    }


    private void onDownloadComplete(){
        notificationManager.cancel(0);
        notificationBuilder.setProgress(0,0,false);
        notificationBuilder.setContentText(this.fileName+" "+getString(R.string.download_complete));
        notificationManager.notify(0, notificationBuilder.build());

        //update media store
        ContentValues values = new ContentValues();
        String artist_text="";
        if(track.getArtists()!=null &&  track.getArtists().size()!=0) {
            ArrayList<Artist> artists = track.getArtists();
            for (int i = 0; i < artists.size(); i++) {
                if(i == (artists.size()-1)){
                    artist_text += artists.get(i).getArtist();
                }else {
                    artist_text += artists.get(i).getArtist() + " , ";
                }
            }
            artist_text=Helpers.trimRightComma(artist_text);
        }else{
            artist_text=getString(R.string.unknown);
        }
        values.put(MediaStore.Audio.Media.ARTIST, artist_text);
        values.put(MediaStore.Audio.Media.ALBUM, getResources().getString(R.string.app_name));
        values.put(MediaStore.Audio.Media.DATA, this.fullFilePath);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.TITLE,track.getTitle());
        values.put(MediaStore.Audio.Media.DURATION,track.getDuration());
        getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(this.fullFilePath), values);

        Intent intent = new Intent(DOWNLOAD_COMPLETED);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        notificationManager.cancel(0);
    }
}
