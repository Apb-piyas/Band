package lrandomdev.com.online.mp3player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.ServicePlayer;

public class ActivityFavorites extends ActivityParent implements
        AdapterTrack.OnTrackClickCallback {
    BoxStore boxStore;
    RecyclerView recyclerView;
    int resources;
    ArrayList<Track> tracks;
    AdapterTrack mAdapter;
    ServicePlayer audioPlayerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        Intent intent = new Intent(this,
                ServicePlayer.class);
        getApplicationContext().bindService(intent, serviceConnection,
                BIND_AUTO_CREATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.favorites));

        boxStore= MainApplication.getApp().getBoxStore();
        recyclerView= (RecyclerView)findViewById(R.id.lvTrack);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityFavorites.this);
        Boolean isGrid = prefs.getBoolean("track_grid", true);
        if(isGrid){
            GridLayoutManager gridLayoutManager=new GridLayoutManager(ActivityFavorites.this,2);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.resources=R.layout.row_track_item_grid;
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivityFavorites.this);
            recyclerView.setLayoutManager(linearLayoutManager);
            this.resources=R.layout.row_track_item;
        }

        tracks = Helpers.getFavoritesList(boxStore);
        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterTrack(ActivityFavorites.this, tracks, this.resources);
        mAdapter.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                audioPlayerService.play(position, tracks);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            audioPlayerService = ((ServicePlayer.PlayerBinder) service)
                    .getService();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onItemClickCallback(int position, ArrayList<Track> tracks) {

    }

    @Override
    public void onItemAddQueue(Track track) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
