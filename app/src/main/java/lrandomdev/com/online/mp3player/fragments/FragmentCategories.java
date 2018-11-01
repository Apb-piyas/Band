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
import lrandomdev.com.online.mp3player.adapters.AdapterCategories;
import lrandomdev.com.online.mp3player.adapters.AdapterCategories;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Categories;
import lrandomdev.com.online.mp3player.models.Categories;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategories extends FragmentParent{
    ArrayList<Categories> categories=new ArrayList<Categories>();
    AdapterCategories mAdapter;
    RecyclerView recyclerView;
    int first=-10;
    int offset=10;
    ApiServices apiServices;
    AVLoadingIndicatorView avLoadingIndicatorView;
    SwipeRefreshLayout swipeRefreshLayout;
    String query = null;

    public static final FragmentCategories newInstance() {
        FragmentCategories fragment = new FragmentCategories();
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
        mAdapter = new AdapterCategories(getActivity(), categories,R.layout.row_album_item_grid,fragmentLibrary);
        mAdapter.setOnItemClickListener(new AdapterCategories.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putInt("type",3);
                bundle.putInt("id",categories.get(position).getId());
                bundle.putString("thumb",categories.get(position).getThumb());
                bundle.putString("title",categories.get(position).getTitle());
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
                if(categories.size()>0) {
                    Call<ArrayList<Categories>> call = apiServices.pullCategories(categories.get(0).getId()+"");
                    call.enqueue(new Callback<ArrayList<Categories>>() {
                        @Override
                        public void onResponse(Call<ArrayList<Categories>> call, Response<ArrayList<Categories>> response) {
                            swipeRefreshLayout.setRefreshing(false);
                            ArrayList<Categories> tmp = response.body();
                            if (tmp != null) {
                                for (int i = 0; i < tmp.size(); i++) {
                                    categories.add(0, tmp.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<ArrayList<Categories>> call, Throwable t) {
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
        categories.clear();
        first=-10;
        loadMore();
    }


    void loadMore(){
        avLoadingIndicatorView.show();
        first+=10;
        Call<ArrayList<Categories>> call= apiServices.getCategories(first,offset,query);
        call.enqueue(new Callback<ArrayList<Categories>>() {
            @Override
            public void onResponse(Call<ArrayList<Categories>> call, Response<ArrayList<Categories>> response) {
                ArrayList<Categories> tmpListPhoto=response.body();
                if(tmpListPhoto!=null) {
                    for (int i = 0; i < tmpListPhoto.size(); i++) {
                        categories.add(tmpListPhoto.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
                avLoadingIndicatorView.hide();
            }

            @Override
            public void onFailure(Call<ArrayList<Categories>> call, Throwable t) {
                avLoadingIndicatorView.hide();
            }
        });
    }
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
    }
}

