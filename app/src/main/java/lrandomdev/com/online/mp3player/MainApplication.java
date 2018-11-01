package lrandomdev.com.online.mp3player;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.telecom.Call;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.FirebaseApp;
import com.google.gson.JsonObject;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.util.prefs.Preferences;

import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.MyObjectBox;
import retrofit2.Callback;
import retrofit2.Response;

public class MainApplication extends Application {
    private BoxStore mBoxStore;
    private static MainApplication sApp;

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = MainApplication.this;
        FirebaseApp.initializeApp(getApplicationContext());
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new NotificationOpenHandler())
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        sAnalytics = GoogleAnalytics.getInstance(this);
        mBoxStore = MyObjectBox.builder().androidContext(MainApplication.this).build();
    }

    public static MainApplication getApp() {
        return sApp;
    }

    public BoxStore getBoxStore() {
        return mBoxStore;
    }

    synchronized public Tracker getDefaultTracker() {
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }
        return sTracker;
    }

    private class NotificationOpenHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotification notification = result.notification;
            JSONObject data = notification.payload.additionalData;
            Intent intent = new Intent(getApplicationContext(), ActivityHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (data != null) {
                int key = Integer.parseInt(data.optString("key"));
                Bundle bundle = new Bundle();
                switch (key) {
                    case 0:
                        intent = new Intent(getApplicationContext(), ActivityTrack.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //if is category
                        bundle.putInt("type", 3);
                        int id = Integer.parseInt(data.optString("id"));
                        String sub_title = data.optString("sub_title");
                        String thumb = RestClient.BASE_URL + data.optString("thumb");
                        bundle.putInt("id", id);
                        bundle.putString("thumb", thumb);
                        bundle.putString("title", sub_title);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;

                    case 1:
                        intent = new Intent(getApplicationContext(), ActivityTrack.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //if is artist
                        bundle.putInt("type", 1);
                        id = Integer.parseInt(data.optString("id"));
                        sub_title = data.optString("sub_title");
                        thumb = RestClient.BASE_URL + data.optString("thumb");
                        bundle.putInt("id", id);
                        bundle.putString("thumb", thumb);
                        bundle.putString("title", sub_title);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;

                    case 2:
                        intent = new Intent(getApplicationContext(), ActivityTrack.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //if is track
                        bundle.putInt("type", 5);
                        id = Integer.parseInt(data.optString("id"));
                        sub_title = data.optString("sub_title");
                        thumb = RestClient.BASE_URL + data.optString("thumb");
                        bundle.putInt("id", id);
                        bundle.putString("thumb", thumb);
                        bundle.putString("title", sub_title);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;

                    case 3:
                        //if external url
                        String url = data.optString("url");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(browserIntent);
                        break;
                }
            }else {
                startActivity(intent);
            }
        }
    }

}
