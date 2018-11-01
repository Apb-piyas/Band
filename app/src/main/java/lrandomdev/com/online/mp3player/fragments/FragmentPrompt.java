package lrandomdev.com.online.mp3player.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.models.MyPlaylist;
import lrandomdev.com.online.mp3player.models.Track;

/**
 * Created by Lrandom on 4/7/18.
 */

public class FragmentPrompt extends DialogFragment {
    EditText inputText = null;
    Track track;
    BoxStore boxStore;

    public static FragmentPrompt newInstance() {
        FragmentPrompt frag = new FragmentPrompt();
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        boxStore = MainApplication.getApp().getBoxStore();
        track = (Track)getArguments().getSerializable("item");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inputText = new EditText(getActivity());
        inputText.setLines(1);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inputText)
                .setTitle(getResources().getString(R.string.create_playlist))
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
        AlertDialog promptDialog = builder.create();
        promptDialog.show();
        promptDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (inputText.getText().length() != 0) {
                            Box<MyPlaylist> boxPlaylist =boxStore.boxFor(MyPlaylist.class);
                            MyPlaylist playlist = new MyPlaylist();
                            playlist.setName(inputText.getText().toString());
                            long playlistId=boxPlaylist.put(playlist);

                            Box<Track> boxTrack = boxStore.boxFor(Track.class);
                            track.setThumb(track.getThumb());
                            track.setPlaylistId(playlistId);
                            boxTrack.put(track);

                            playlist.setThumb(track.getThumb());
                            playlist.setTotal_track(playlist.getTotal_track()+1);
                            boxPlaylist.put(playlist);

                            Toast ts=Toast.makeText(getActivity(),track.getTitle()+" "+ getString(R.string.song_had_been_saved)+" "+playlist.getName(),Toast.LENGTH_SHORT);
                            ts.show();
                            dismiss();
                        }
                    }
                });
        return promptDialog;
    }
}
