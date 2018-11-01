package lrandomdev.com.online.mp3player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Locale;

import lrandomdev.com.online.mp3player.services.DownloadService;

/**
 * Created by Lrandom on 4/23/18.
 */

public class ActivityParent extends AppCompatActivity{
    MyBroadCastReceiver broadCastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int somePrefValue = Integer.valueOf(prefs.getString("themes", "0"));
        switch (somePrefValue){
            case 0:
                setTheme(R.style.PurpeTheme);
                break;

            case 1:
                setTheme(R.style.OrangeTheme);
                break;

        }

        String lang = prefs.getString("language", "en");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale= locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());



        broadCastReceiver =new MyBroadCastReceiver();
        IntentFilter intentFilter =new IntentFilter();
        intentFilter.addAction(DownloadService.DOWNLOAD_COMPLETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver);
    }

    class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getBaseContext(),getString(R.string.download_complete),Toast.LENGTH_SHORT).show();
        }
    }
}
