package lrandomdev.com.online.mp3player.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityTrack;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterPlaylist;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Playlist;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPlaylist extends FragmentParent {
    ArrayList<Playlist> playlists = new ArrayList<Playlist>();
    AdapterPlaylist mAdapter;
    RecyclerView recyclerView;
    int resources;
    int first = -10;
    int offset = 10;
    ApiServices apiServices;
    AVLoadingIndicatorView avLoadingIndicatorView;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;

    public static FragmentPlaylist newInstance() {
        FragmentPlaylist fragment = new FragmentPlaylist();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle_view,
                container, false);
        apiServices = RestClient.getApiService();
        avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.loadingView);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.resources = R.layout.row_playlist_item_grid;
        recyclerView.setHasFixedSize(true);
        FragmentLibrary fragmentLibrary = ((FragmentLibrary) getParentFragment());
        mAdapter = new AdapterPlaylist(getActivity(), playlists, this.resources, fragmentLibrary);
        mAdapter.setOnItemClickListener(new AdapterPlaylist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("type", 2);
                bundle.putInt("id", (int) playlists.get(position).getId());
                bundle.putString("thumb", playlists.get(position).getThumb());
                bundle.putString("title", playlists.get(position).getName());
                Intent intent = new Intent(getContext(), ActivityTrack.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(mAdapter);
        loadMore();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (playlists.size() > 0) {
                    Call<ArrayList<Playlist>> call = apiServices.pullPlaylists(playlists.get(0).getId() + "");
                    call.enqueue(new Callback<ArrayList<Playlist>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Playlist> tmp = response.body();
                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    playlists.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMore();
            }
        });
        return view;
    }

    @Override
    public void putArguments(Bundle args) {
        super.putArguments(args);
        query = args.getString("query");
        playlists.clear();
        first = -10;
        loadMore();
    }


    void loadMore() {
        avLoadingIndicatorView.show();
        first += 10;
        Call<ArrayList<Playlist>> call = apiServices.getPlaylists(first, offset, query);
        call.enqueue(new Callback<ArrayList<Playlist>>() {
            @Override
            public void onResponse(Call<ArrayList<Playlist>> call, Response<ArrayList<Playlist>> response) {
                ArrayList<Playlist> tmpListPhoto = response.body();
                if (tmpListPhoto != null) {
                    for (int i = 0; i < tmpListPhoto.size(); i++) {
                        playlists.add(tmpListPhoto.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
                avLoadingIndicatorView.hide();
            }

            @Override
            public void onFailure(Call<ArrayList<Playlist>> call, Throwable t) {
                avLoadingIndicatorView.hide();
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }
}
