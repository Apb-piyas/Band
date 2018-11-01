package lrandomdev.com.online.mp3player.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import lrandomdev.com.online.mp3player.adapters.AdapterArtist;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Artist;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentArtist extends FragmentParent {
    ArrayList<Artist> artists=new ArrayList<Artist>();
    AdapterArtist mAdapter;
    RecyclerView recyclerView;
    int first=-10;
    int offset=10;
    ApiServices apiServices;
    AVLoadingIndicatorView avLoadingIndicatorView;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;

    public static final FragmentArtist newInstance() {
        FragmentArtist fragment = new FragmentArtist();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle_view,
                container, false);
        apiServices= RestClient.getApiService();
        avLoadingIndicatorView=(AVLoadingIndicatorView)view.findViewById(R.id.loadingView);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        FragmentLibrary fragmentLibrary=(FragmentLibrary)getParentFragment();
        mAdapter = new AdapterArtist(getActivity(), artists,fragmentLibrary);
        mAdapter.setOnItemClickListener(new AdapterArtist.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("type",1);
                bundle.putInt("id",artists.get(position).getId());
                bundle.putString("thumb",artists.get(position).getThumb());
                bundle.putString("title",artists.get(position).getArtist());
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
                if(artists.size()>0) {
                    Call<ArrayList<Artist>> call = apiServices.pullArtist(artists.get(0).getId()+"");
                    call.enqueue(new Callback<ArrayList<Artist>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Artist>> call, Response<ArrayList<Artist>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Artist> tmp = response.body();
                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    artists.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Artist>> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
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
        artists.clear();
        first=-10;
        loadMore();
    }


    void loadMore(){
        avLoadingIndicatorView.show();
        first+=10;
        Call<ArrayList<Artist>> call= apiServices.getArtists(first,offset,query);
        call.enqueue(new Callback<ArrayList<Artist>>() {
            @Override
            public void onResponse(Call<ArrayList<Artist>> call, Response<ArrayList<Artist>> response) {
                ArrayList<Artist> tmpListPhoto=response.body();
                if(tmpListPhoto!=null) {
                    for (int i = 0; i < tmpListPhoto.size(); i++) {
                        artists.add(tmpListPhoto.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
                avLoadingIndicatorView.hide();
            }

            @Override
            public void onFailure(Call<ArrayList<Artist>> call, Throwable t) {
                avLoadingIndicatorView.hide();
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }
}
