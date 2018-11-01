package lrandomdev.com.online.mp3player.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterLocalTrack;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.MyFile;
import lrandomdev.com.online.mp3player.models.Track;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentDownload extends FragmentParent {
    ArrayList<Track> tracks = new ArrayList<Track>();
    RecyclerView recyclerView;
    AdapterLocalTrack mAdapter;
    FragmentLibrary fragmentLibrary;
    int resources;
    AVLoadingIndicatorView avLoadingIndicatorView;

    public static final FragmentDownload newInstance() {
        FragmentDownload fragment = new FragmentDownload();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tracks = new ArrayList<Track>();
        View view = inflater.inflate(R.layout.fragment_tracks,
                container, false);
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.loadingView);
        avLoadingIndicatorView.hide();
        loadFiles();
        fragmentLibrary = ((FragmentLibrary) getParentFragment());
        recyclerView = (RecyclerView)view.findViewById(R.id.lvTrack);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isGrid = prefs.getBoolean("track_grid", true);
        if(isGrid){
            GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.resources=R.layout.row_track_item_grid;
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            this.resources=R.layout.row_track_item;
        }
        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterLocalTrack(getActivity(), tracks, fragmentLibrary, this.resources);
        mAdapter.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                fragmentLibrary.trackClickItem(tracks,position);
            }
        });
        recyclerView.setAdapter(mAdapter);
        final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tracks.clear();
                loadFiles();
                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }

    public void loadFiles(){
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ File.separator + getString(R.string.app_name).replace(" ","");
        final File folders = new File(dir);
        folders.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg")) {
                    MyFile fileTrack = new MyFile(pathname.getPath());
                    tracks.add(Helpers.getTrackFromAbPath(getContext(),fileTrack));
                }
                return false;
            }
        });
    }

}
