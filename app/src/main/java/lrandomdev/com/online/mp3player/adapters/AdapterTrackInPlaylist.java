package lrandomdev.com.online.mp3player.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.fragments.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.DownloadService;


/**
 * Created by Lrandom on 4/8/18.
 */

public class AdapterTrackInPlaylist extends AdapterTrack {

    public AdapterTrackInPlaylist(Context context, ArrayList<Track> tracks, int resources) {
        super(context, tracks, resources);
    }

    public AdapterTrackInPlaylist(Context context, ArrayList<Track> tracks, FragmentLibrary fragmentLibrary, int resources) {
        super(context, tracks, fragmentLibrary, resources);
    }


    @Override
    public void showMenu(final View view) {
        PopupMenu menu = new PopupMenu(getContext(), view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                final Track track = (Track) view.getTag();
                switch (id) {
                    case R.id.item_play:
                        if (fragmentLibrary != null) {
                            fragmentLibrary.trackClickItem(getTracks(), getTracks().indexOf(track));
                        } else {
                            mTrackClickCallback.onItemClickCallback(getTracks().indexOf(track), getTracks());
                        }
                        break;

                    case R.id.item_add_to_queue:
                        if (fragmentLibrary != null) {
                            fragmentLibrary.trackAddToQueue(track);
                        } else {
                            mTrackClickCallback.onItemAddQueue(track);
                        }
                        break;

                    case R.id.item_download:
                        Intent intent = new Intent(getContext(), DownloadService.class);
                        intent.putExtra("file", track);
                        getContext().startService(intent);
                        //Toast.makeText(getContext(),"This not avaiable in demo",Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.item_to_playlist:
                        FragmentSelectPlaylistDialog newFragment = FragmentSelectPlaylistDialog
                                .newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("item", track);
                        newFragment.setArguments(bundle);
                        Activity activity = (Activity) getContext();
                        newFragment.show(activity.getFragmentManager(), "dialog");
                        break;

                    case R.id.item_share:
                        Helpers.shareAction(getContext(), track);
                        break;

                    case R.id.item_remove_from_playlist:
                        Helpers.removeTrackInPlaylist(boxStore, track);
                        getTracks().remove(track);
                        notifyDataSetChanged();
                        break;
                }
                return true;
            }
        });
        menu.inflate(R.menu.menu_track_in_playlist);
        menu.show();

        SharedPreferences prefs = getContext().getSharedPreferences("allow_download",Context.MODE_PRIVATE);
        int isAllow= prefs.getInt("is_allow",0);
        if(isAllow==0){
            menu.getMenu().findItem(R.id.item_download).setVisible(false);
        }else{
            menu.getMenu().findItem(R.id.item_download).setVisible(true);
        }
    }
}
