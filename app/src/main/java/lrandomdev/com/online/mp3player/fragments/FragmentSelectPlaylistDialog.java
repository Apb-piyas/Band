package lrandomdev.com.online.mp3player.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterPlaylistSelect;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.MyPlaylist;
import lrandomdev.com.online.mp3player.models.Playlist;
import lrandomdev.com.online.mp3player.models.Track;
/**
 * Created by Lrandom on 11/19/16.
 */
public class FragmentSelectPlaylistDialog extends android.app.DialogFragment {
    ArrayList<MyPlaylist> myPlaylists;
    ArrayList<MyPlaylist> selectedMyPlaylists;
    AdapterPlaylistSelect adapterMyPlaylist;
    Track track;
    BoxStore boxStore;

    public static final FragmentSelectPlaylistDialog newInstance() {
        FragmentSelectPlaylistDialog fragment = new FragmentSelectPlaylistDialog();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        track = (Track)getArguments().getSerializable("item");
        boxStore = MainApplication.getApp().getBoxStore();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        Box<MyPlaylist> box = boxStore.boxFor(MyPlaylist.class);
        myPlaylists =  new ArrayList<MyPlaylist>(box.getAll());
        selectedMyPlaylists = new ArrayList<MyPlaylist>();
        adapterMyPlaylist = new AdapterPlaylistSelect(getActivity(),
                R.layout.row_select_playlist, myPlaylists);
        ListView list = new ListView(getActivity());
        list.setAdapter(adapterMyPlaylist);
        list.setItemsCanFocus(false);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setDivider(null);
        list.setDividerHeight(0);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                CheckBox cbox = (CheckBox) view.findViewById(R.id.cbox);
                if (cbox.isChecked()) {
                   myPlaylists.get(arg2).setChecked(false);
                    selectedMyPlaylists.remove(myPlaylists.get(arg2));
                    cbox.setChecked(false);
                } else {
                    cbox.setChecked(true);
                    myPlaylists.get(arg2).setChecked(true);
                    selectedMyPlaylists.add(myPlaylists.get(arg2));
                };
            }
        });

        builder.setTitle(getResources().getString(R.string.select_playlist))
                .setView(list)
                .setNeutralButton(R.string.create_playlist, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentPrompt fragmentPrompt= FragmentPrompt.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("item",track);
                        fragmentPrompt.setArguments(bundle);
                        fragmentPrompt.show(getActivity().getFragmentManager(), "dialog");
                    }
                })
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (!selectedMyPlaylists.isEmpty()) {
                            //save to MyPlaylist
                            Box<Track> trackBox = boxStore.boxFor(Track.class);
                            Box<MyPlaylist> playlistBox = boxStore.boxFor(MyPlaylist.class);
                            for (int i = 0; i < selectedMyPlaylists.size() ; i++) {
                                MyPlaylist myPlaylist =selectedMyPlaylists.get(i);
                                myPlaylist.setThumb(track.getThumb());
                                myPlaylist.setTotal_track(myPlaylist.getTotal_track()+1);
                                playlistBox.put(myPlaylist);
                                track.setThumb(track.getThumb().replace(RestClient.BASE_URL,""));
                                track.setPlaylistId(myPlaylist.getId());
                                trackBox.put(track);
                            }

                            //end save MyPlaylist
                            dismiss();
                        }
                    }
                });
        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
