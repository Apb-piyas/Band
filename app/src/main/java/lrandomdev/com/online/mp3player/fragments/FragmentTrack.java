package lrandomdev.com.online.mp3player.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Track;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentTrack extends FragmentParent{
    ArrayList<Track> tracks=new ArrayList<Track>();
    AdapterTrack mAdapter;
    RecyclerView recyclerView;
    FragmentLibrary fragmentLibrary;
    ApiServices apiServices;
    int first=-10;
    int offset=10;
    AVLoadingIndicatorView avLoadingIndicatorView;
    int resources;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;

    public static final FragmentTrack newInstance() {
        FragmentTrack fragment = new FragmentTrack();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks,
                container, false);
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.loadingView);
        apiServices= RestClient.getApiService();

        fragmentLibrary = ((FragmentLibrary) getParentFragment());
        recyclerView= (RecyclerView)view.findViewById(R.id.lvTrack);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean isGrid = prefs.getBoolean("track_grid", true);
        if(isGrid){
            GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.resources=R.layout.row_track_item_grid;
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    loadMore();
                }
            });
        }else{
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            this.resources=R.layout.row_track_item;
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    loadMore();
                }
            });
        }

        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterTrack(getActivity(), tracks, fragmentLibrary, this.resources);
        mAdapter.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                fragmentLibrary.trackClickItem(tracks,position);
            }
        });

        recyclerView.setAdapter(mAdapter);
        loadMore();

        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(tracks.size()>0) {
                    Call<ArrayList<Track>> call = apiServices.pullTrack(tracks.get(0).getRemoteId());
                    call.enqueue(new Callback<ArrayList<Track>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Track> tmp = response.body();

                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    tracks.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Track>> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });


        return view;
    }

    @Override
    public void putArguments(Bundle args) {
        super.putArguments(args);
        query = args.getString("query");
        tracks.clear();
        first=-10;
        loadMore();
    }

    void loadMore(){
        avLoadingIndicatorView.show();
        first+=10;
        Call<ArrayList<Track>> call= apiServices.getTracks(first,offset,query);
        call.enqueue(new Callback<ArrayList<Track>>() {
            @Override
            public void onResponse(Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                ArrayList<Track> tmpListPhoto=response.body();
                Log.e("TMP",tmpListPhoto.size()+"");
                if(tmpListPhoto!=null) {
                    for (int i = 0; i < tmpListPhoto.size(); i++) {
                        tracks.add(tmpListPhoto.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
                avLoadingIndicatorView.hide();
            }

            @Override
            public void onFailure(Call<ArrayList<Track>> call, Throwable t) {
                Log.e("ERROR",t.getMessage().toString());
                avLoadingIndicatorView.hide();
            }
        });
    }
}
