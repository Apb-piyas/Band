package lrandomdev.com.online.mp3player;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.MobileAds;
import com.google.gson.JsonObject;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Lrandom on 3/23/18.
 */

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Permissions.check(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    "", new Permissions.Options()
                            .setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                    new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            //do your task
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent startActivityIntent = new Intent(ActivitySplash.this, ActivityHome.class);
                                    startActivity(startActivityIntent);
                                    ActivitySplash.this.finish();
                                }
                            },1000);
                        }

                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            super.onDenied(context, deniedPermissions);
                            finish();
                        }
                    });
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent startActivityIntent = new Intent(ActivitySplash.this, ActivityHome.class);
                    startActivity(startActivityIntent);
                    ActivitySplash.this.finish();
                }
            },1000);
        }
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
            actionBar.hide();
        }
    }

}
