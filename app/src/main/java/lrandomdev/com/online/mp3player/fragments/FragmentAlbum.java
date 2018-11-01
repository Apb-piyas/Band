package lrandomdev.com.online.mp3player.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.ActivityTrack;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.AdapterAlbum;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Album;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentAlbum extends FragmentParent{
    ArrayList<Album> albums=new ArrayList<Album>();
    AdapterAlbum mAdapter;
    RecyclerView recyclerView;
    int first=-10;
    int offset=10;
    ApiServices apiServices;
    AVLoadingIndicatorView avLoadingIndicatorView;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;

    public static final FragmentAlbum newInstance() {
        FragmentAlbum fragment = new FragmentAlbum();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle_view,
                container, false);
        apiServices= RestClient.getApiService();
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.loadingView);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        FragmentLibrary fragmentLibrary=  ((FragmentLibrary) getParentFragment());
        mAdapter = new AdapterAlbum(getActivity(), albums,R.layout.row_album_item_grid,fragmentLibrary);
        mAdapter.setOnItemClickListener(new AdapterAlbum.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("type",0);
                bundle.putInt("id",albums.get(position).getId());
                bundle.putString("thumb",albums.get(position).getThumb());
                bundle.putString("title",albums.get(position).getTitle());
                Intent intent = new Intent(getContext(), ActivityTrack.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(mAdapter);
        loadMore();
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(albums.size()>0) {
                    Call<ArrayList<Album>> call = apiServices.pullAlbum(albums.get(0).getId()+"");
                    call.enqueue(new Callback<ArrayList<Album>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Album>> call, Response<ArrayList<Album>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Album> tmp = response.body();
                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    albums.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Album>> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else {
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
        albums.clear();
        first=-10;
        loadMore();
    }


    void loadMore(){
        avLoadingIndicatorView.show();
        first+=10;
        Call<ArrayList<Album>> call= apiServices.getAlbums(first,offset,query);
        call.enqueue(new Callback<ArrayList<Album>>() {
            @Override
            public void onResponse(Call<ArrayList<Album>> call, Response<ArrayList<Album>> response) {
                ArrayList<Album> tmpListPhoto=response.body();
                if(tmpListPhoto!=null) {
                    for (int i = 0; i < tmpListPhoto.size(); i++) {
                        albums.add(tmpListPhoto.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
                avLoadingIndicatorView.hide();
            }

            @Override
            public void onFailure(Call<ArrayList<Album>> call, Throwable t) {
                avLoadingIndicatorView.hide();
            }
        });
    }
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }
}
