package lrandomdev.com.online.mp3player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.gson.JsonObject;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import jp.wasabeef.glide.transformations.BlurTransformation;
import lrandomdev.com.online.mp3player.adapters.AdapterTrack;
import lrandomdev.com.online.mp3player.adapters.AdapterTrackInPlaylist;
import lrandomdev.com.online.mp3player.adapters.AdapterTrackInQueue;
import lrandomdev.com.online.mp3player.fragments.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.helpers.SimpleItemTouchHelperCallback;
import lrandomdev.com.online.mp3player.interfaces.EndlessRecyclerViewScrollListener;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.models.Track_;
import lrandomdev.com.online.mp3player.services.ServicePlayer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/31/18.
 */

public class ActivityTrack extends ActivityParent implements
        AdapterTrack.OnTrackClickCallback,
        SeekBar.OnSeekBarChangeListener {
    ArrayList<Track> tracks = new ArrayList<Track>();
    ArrayList<Track> showTracks = new ArrayList<Track>();
    ImageView imgThumbPreview;
    FloatingActionButton fab;

    private SlidingUpPanelLayout slidingUpPanelLayout;
    LinearLayout miniPlayback;
    SlidingUpPanelLayout.PanelState oldPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    ServicePlayer audioPlayerService;
    Handler handler = new Handler();
    TextView tvTitle, tvTotalTime, tvElapsedTime, tvArtist, tvMiniTitle, tvMiniArtist;
    ImageButton btnPlayAndPause, btnNext, btnPrev, btnRepeat, btnShuffle,
            btnPlaylist, btnShare,btnLyrics, btnAddToPlaylist, btnMiniPrev, btnMiniNext, btnMiniPlayAndPause;
    SeekBar prgTrack;
    boolean isRepeat, isShuffle;
    int trackIndex;
    String thumb;
    CircleImageView imgThumb;
    FrameLayout frameLayout;
    ImageView imgBg;
    ImageButton btnBack;
    Animation rotation, rotationMenuDisk;
    ImageView imgMiniThumb;
    AdapterTrackInQueue adapterTrackInQueue;
    private ItemTouchHelper mItemTouchHelper;
    RecyclerView songRecyclerView;
    Call<ArrayList<Track>> call;
    int first = -10;
    int offset = 10;
    ApiServices apiService;
    AdapterTrack mAdapter;

    String lyrics="";
    AVLoadingIndicatorView indicator,mainIndicator;

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ServicePlayer.UPDATE_UI:
                    updatePlaybackUI();
                    indicator.setVisibility(View.GONE);
                    mainIndicator.setVisibility(View.GONE);

                    btnMiniPlayAndPause.setVisibility(View.VISIBLE);
                    btnPlayAndPause.setVisibility(View.VISIBLE);
                    break;

                case ServicePlayer.BUFFERING:
                    indicator.setVisibility(View.VISIBLE);
                    mainIndicator.setVisibility(View.VISIBLE);

                    imgMiniThumb.setImageResource(R.drawable.bg);
                    imgThumb.setImageResource(R.drawable.bg);
                    imgThumb.refreshDrawableState();

                    Glide.with(getApplicationContext()).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);


                    tvMiniTitle.setText(getString(R.string.loading));
                    tvMiniArtist.setText(getString(R.string.loading));
                    tvArtist.setText(getString(R.string.loading));
                    tvArtist.setText(getString(R.string.loading));

                    btnMiniPlayAndPause.setVisibility(View.INVISIBLE);
                    btnPlayAndPause.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        Toolbar toolbar =(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiService  = RestClient.getApiService();
        Intent intent = new Intent(this,
                ServicePlayer.class);
        getApplicationContext().bindService(intent, serviceConnection,
                BIND_AUTO_CREATE);

        imgThumbPreview = (ImageView) findViewById(R.id.imgThumbPreview);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.htab_collapse_toolbar);

        songRecyclerView = (RecyclerView) findViewById(R.id.lvTrack);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        songRecyclerView.setLayoutManager(linearLayoutManager);
        songRecyclerView.setHasFixedSize(true);

        Bundle bundle = getIntent().getExtras();
        final int type = bundle.getInt("type");
        final int id = bundle.getInt("id");
        String title = bundle.getString("title");
        String thumb = bundle.getString("thumb");

        Boolean isTrack = bundle.getBoolean("is_track");

        collapsingToolbarLayout.setTitle(title);

        if (thumb != null) {
            Glide.with(ActivityTrack.this).load(thumb).into(imgThumbPreview);
        }

        if(type!=4){
            loadMore(type,id+"");

            mAdapter = new AdapterTrack(ActivityTrack.this, showTracks,R.layout.row_track_item);
            mAdapter.setOnItemClickListener(new AdapterTrack.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    audioPlayerService.play(position, showTracks);
                }
            });
            songRecyclerView.setAdapter(mAdapter);

            songRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                   loadMore(type,id+"");
                }
            });

        }else{
            BoxStore boxStore =MainApplication.getApp().getBoxStore();
            Box<Track> box = boxStore.boxFor(Track.class);
            showTracks= new ArrayList<>(box.query().equal(Track_.playlistId,id).build().find());
            AdapterTrackInPlaylist mAdapter = new AdapterTrackInPlaylist(ActivityTrack.this,
                    showTracks,R.layout.row_track_item);
            mAdapter.setOnItemClickListener(new AdapterTrackInPlaylist.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    audioPlayerService.play(position, showTracks);
                }
            });
            songRecyclerView.setAdapter(mAdapter);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showTracks==null || showTracks.size() == 0) {
                    Toast ts = Toast.makeText(
                            ActivityTrack.this, getString(R.string.do_not_have_any_song_to_play), Toast.LENGTH_SHORT);
                    ts.show();
                } else {
                    audioPlayerService.play(0, showTracks);
                }
            }
        });

        presetMainUI();
        presetPlaybackUI();
        if (audioPlayerService != null) {
            updatePlaybackUI();
        }

        Helpers.loadInAd(getApplicationContext());
    }

    void loadMore(int type,String id){
        first+=10;
        switch (type) {
            case 0:
                call = apiService.getTracks(first,offset,null,id,null,null);
                break;

            case 1:
                call = apiService.getTracks(first,offset,null,null,id,null);
                break;

            case 2:
                call = apiService.getTracks(first,offset,null,null,null,id);
                break;

            case 3:
                call = apiService.getTracks(first,offset,id,null,null,null);
                break;

            case 5:
                //if is push load track
                call = apiService.getTracksById(id);
                break;
        }
        call.enqueue(new Callback<ArrayList<Track>>() {
            @Override
            public void onResponse(retrofit2.Call<ArrayList<Track>> call, Response<ArrayList<Track>> response) {
                //showTracks=response.body();
                for (int i =0; i<response.body().size();i++){
                    showTracks.add(response.body().get(i));
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(retrofit2.Call<ArrayList<Track>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServicePlayer.UPDATE_UI);
        intentFilter.addAction(ServicePlayer.BUFFERING);
        registerReceiver(notificationReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (audioPlayerService != null) {
            updatePlaybackUI();
        }
    }

    void presetMainUI() {
        miniPlayback = (LinearLayout) findViewById(R.id.miniPlayback);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (oldPanelState == SlidingUpPanelLayout.PanelState.COLLAPSED && slideOffset > 0.5) {
                    miniPlayback.animate()
                            .alpha(0.0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    miniPlayback.setVisibility(View.GONE);
                                }
                            });
                }

                if (oldPanelState == SlidingUpPanelLayout.PanelState.EXPANDED && slideOffset < 0.5) {
                    miniPlayback.animate()
                            .alpha(1.0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    miniPlayback.setVisibility(View.VISIBLE);
                                }
                            });
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    oldPanelState = SlidingUpPanelLayout.PanelState.EXPANDED;
                }

                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    oldPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
                }
            }
        });


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
            updatePlaybackUI();

        }
    };

    @Override
    public void onItemClickCallback(int position, ArrayList<Track> tracks) {
        audioPlayerService.play(position, tracks);
    }


    public void presetPlaybackUI() {
        indicator=(AVLoadingIndicatorView)findViewById(R.id.indicator);
        mainIndicator=(AVLoadingIndicatorView)findViewById(R.id.mainIndicator);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvArtist = (TextView) findViewById(R.id.tvArtist);
        tvMiniArtist = (TextView) findViewById(R.id.tvMiniArtist);
        tvMiniTitle = (TextView) findViewById(R.id.tvMiniTitle);
        btnMiniNext = (ImageButton) findViewById(R.id.btnMiniNext);
        btnMiniPrev = (ImageButton) findViewById(R.id.btnMiniPrev);
        btnMiniPlayAndPause = (ImageButton) findViewById(R.id.btnMiniPlayPause);
        imgMiniThumb = (ImageView) findViewById(R.id.imgMiniThumb);

        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        tvElapsedTime = (TextView) findViewById(R.id.tvElapsedTime);
        prgTrack = (SeekBar) findViewById(R.id.seekTrack);
        btnPlayAndPause = (ImageButton) findViewById(R.id.btnPlay);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        imgThumb = (CircleImageView) findViewById(R.id.imgThumb);
        imgBg = (ImageView) findViewById(R.id.bg);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        frameLayout = (FrameLayout) findViewById(R.id.frameAlbum);


        rotation = AnimationUtils.loadAnimation(ActivityTrack.this, R.anim.rotation);
        rotation.setFillEnabled(true);
        rotation.setFillAfter(true);

        rotationMenuDisk = AnimationUtils.loadAnimation(ActivityTrack.this, R.anim.rotation);
        rotationMenuDisk.setFillEnabled(true);
        rotationMenuDisk.setFillAfter(true);

        btnPlaylist = (ImageButton) findViewById(R.id.btnList);
        btnShare = (ImageButton) findViewById(R.id.btnShare);
        btnAddToPlaylist = (ImageButton) findViewById(R.id.btnAddToPlaylist);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnLyrics = (ImageButton) findViewById(R.id.btnLyrics);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tracks.get(trackIndex).getRemoteId()==null) {
                    Helpers.shareAction(ActivityTrack.this, tracks.get(trackIndex));
                }else{
                    Helpers.shareAction(ActivityTrack.this, RestClient.BASE_URL+"detail?id="+tracks.get(trackIndex).getRemoteId());
                }
            }
        });


        btnAddToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSelectPlaylistDialog newFragment = FragmentSelectPlaylistDialog
                        .newInstance();
                Bundle bundle = new Bundle();
                bundle.putSerializable("item", tracks.get(trackIndex));
                newFragment.setArguments(bundle);
                newFragment.show(ActivityTrack.this.getFragmentManager(), "dialog");
            }
        });



        btnLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogPlus dialog = DialogPlus.newDialog(ActivityTrack.this)
                        .setContentHolder(new ViewHolder(R.layout.layout_lyrics))
                        .setContentBackgroundResource(R.drawable.border_white)
                        .setExpanded(true, 800)
                        .create();
                ScrollView views = (ScrollView) dialog.getHolderView();
                final TextView tvContent = (TextView) views.findViewById(R.id.tvContent);
                final LinearLayout indicator = (LinearLayout) views.findViewById(R.id.wrapIndicator);

                lyrics = tracks.get(trackIndex).getDescription();
                if(lyrics.equalsIgnoreCase("")) {
                    String title = tracks.get(trackIndex).getTitle();
                    ArrayList<Artist> artists = tracks.get(trackIndex).getArtists();
                    indicator.setVisibility(View.VISIBLE);
                    if (artists != null && artists.size() != 0) {
                        Call<JsonObject> call = apiService.getLyrics(RestClient.BASE_URL + "lyrics.php", "json", artists.get(0).getArtist(), title);
                        call.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                JsonObject jsonObject = response.body();
                                Log.e("F", jsonObject.toString());
                                lyrics = jsonObject.get("lyrics").getAsString();
                                if (lyrics.equalsIgnoreCase("")) {
                                    lyrics = getString(R.string.not_found_lyrics);
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    tvContent.setText(Html.fromHtml(lyrics));
                                }
                                indicator.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                lyrics = getString(R.string.not_found_lyrics);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    tvContent.setText(Html.fromHtml(lyrics));
                                }
                                indicator.setVisibility(View.GONE);
                            }
                        });
                    }else{
                        lyrics = getString(R.string.not_found_lyrics);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            tvContent.setText(Html.fromHtml(lyrics));
                        }
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvContent.setText(Html.fromHtml(lyrics, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        tvContent.setText(Html.fromHtml(lyrics));
                    }
                }

                dialog.show();
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });




        btnMiniPlayAndPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayerService.isPlay()) {
                    audioPlayerService.pause();
                    btnMiniPlayAndPause
                            .setImageResource(R.drawable.ic_play_no_circle);
                    btnPlayAndPause
                            .setImageResource(R.drawable.ic_play);
                } else {
                    audioPlayerService.resume();
                    btnMiniPlayAndPause
                            .setImageResource(R.drawable.ic_stop_no_circle);
                    btnPlayAndPause
                            .setImageResource(R.drawable.ic_stop);
                }
            }
        });

        btnMiniPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShuffle) {
                    if (trackIndex == 0) {
                        trackIndex = tracks.size() - 1;
                    } else {
                        trackIndex -= 1;
                    }
                } else {
                    Random rand = new Random();
                    trackIndex = rand.nextInt((tracks.size() - 1) - 0 + 1) + 0;
                }
                audioPlayerService.play(trackIndex, tracks);
            }
        });

        btnMiniNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isShuffle) {
                    if (trackIndex == (tracks.size() - 1)) {
                        trackIndex = 0;
                    } else {
                        trackIndex += 1;
                    }
                } else {
                    Random rand = new Random();
                    trackIndex = rand.nextInt((tracks.size() - 1) - 0 + 1) + 0;
                }
                audioPlayerService.play(trackIndex, tracks);
            }
        });

        btnPlayAndPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMiniPlayAndPause.callOnClick();
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMiniNext.callOnClick();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMiniPrev.callOnClick();
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShuffle) {
                    isShuffle = true;
                    audioPlayerService.setShuffle(true);
                    btnShuffle
                            .setImageResource(R.drawable.ic_shuffle_gr);
                } else {
                    isShuffle = false;
                    audioPlayerService.setShuffle(false);
                    btnShuffle.setImageResource(R.drawable.ic_shuffle);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!isRepeat) {
                    isRepeat = true;
                    audioPlayerService.setRepeat(true);
                    btnRepeat
                            .setImageResource(R.drawable.ic_repeat_gr);
                } else {
                    isRepeat = false;
                    audioPlayerService.setRepeat(false);
                    btnRepeat.setImageResource(R.drawable.ic_repeat);
                }
            }
        });


        LinearLayout adView = (LinearLayout) findViewById(R.id.adView);
        Helpers.loadAd(getApplicationContext(),adView);
    }




    public void updatePlaybackUI() {
        tracks=audioPlayerService.getTracks();
        if (tracks != null) {
            trackIndex = audioPlayerService.getTrackIndex();
            Track track =tracks.get(trackIndex);
            tvTitle.setText(track.getTitle());

            String artist_text = "";
            ArrayList<Artist> artists = track.getArtists();
            if(artists!=null &&  artists.size()!=0) {
                for (int i = 0; i < artists.size(); i++) {
                    if(i == (artists.size()-1)){
                        artist_text += artists.get(i).getArtist();
                    }else {
                        artist_text += artists.get(i).getArtist() + " , ";
                    }
                }
                tvArtist.setText(Helpers.trimRightComma(artist_text));
                tvMiniArtist.setText(Helpers.trimRightComma(artist_text));
            }else {
                tvArtist.setText(track.getArtist());
                tvMiniArtist.setText(track.getArtist());
            }

            if (audioPlayerService.isPlay()) {
                btnMiniPlayAndPause.setImageResource(R.drawable.ic_stop_no_circle);
                btnPlayAndPause.setImageResource(R.drawable.ic_stop);
                frameLayout.startAnimation(rotation);

            } else {
                btnMiniPlayAndPause.setImageResource(R.drawable.ic_play_no_circle);
                btnPlayAndPause.setImageResource(R.drawable.ic_play);
                frameLayout.clearAnimation();
            }

            tvMiniTitle.setText(track.getTitle());
            if (tracks.get(trackIndex).getThumb()!=null) {
                thumb = track.getThumb();
                thumb= RestClient.BASE_URL+thumb;
                if (thumb != null) {
                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgMiniThumb);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgThumb);


                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30,15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                } else {
                    imgMiniThumb.setImageResource(R.drawable.bg);
                    imgThumb.setImageResource(R.drawable.bg);
                    imgThumb.refreshDrawableState();

                    Glide.with(this).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30,15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);
                }
            }


            adapterTrackInQueue = new AdapterTrackInQueue(ActivityTrack.this, tracks);
            btnPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogPlus dialog = DialogPlus.newDialog(ActivityTrack.this)
                            .setContentHolder(new ViewHolder(R.layout.layout_queue))
                            .setContentBackgroundResource(R.drawable.border_white)
                            .setExpanded(true, 800)
                            .create();

                    RecyclerView listQueue = (RecyclerView) dialog.getHolderView();
                    listQueue.setAdapter(adapterTrackInQueue);
                    listQueue.setLayoutManager(new LinearLayoutManager(ActivityTrack.this));
                    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapterTrackInQueue);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(listQueue);
                    adapterTrackInQueue.setOnDragListener(new AdapterTrackInQueue.OnStartDragListener() {
                        @Override
                        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                            mItemTouchHelper.startDrag(viewHolder);
                        }
                    });
                    adapterTrackInQueue.setOnItemClickListener(new AdapterTrackInQueue.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            audioPlayerService.play(position, tracks);
                        }
                    });
                    adapterTrackInQueue.setOnPlayListener(new AdapterTrackInQueue.OnPlayListener() {
                        @Override
                        public void onItemClickCallback(int position, ArrayList<Track> tracks) {
                            audioPlayerService.play(position, tracks);
                        }
                    });
                    dialog.show();
                }
            });
            prgTrack.setOnSeekBarChangeListener(this);
            updateProgress();
        }
    }

    private Runnable updateTime = new Runnable() {
        public void run() {
            prgTrack.setSecondaryProgress(audioPlayerService
                    .getBufferingDownload());
            long totalDuration = audioPlayerService.getTotalTime();
            long currentDuration = audioPlayerService.getElapsedTime();
            tvTotalTime.setText("" + Helpers.timer(totalDuration));
            tvElapsedTime.setText("" + Helpers.timer(currentDuration));
            int progress = (int) (Helpers.getProgressPercentage(currentDuration,
                    totalDuration));
            prgTrack.setProgress(progress);
            handler.postDelayed(this, 100);
        }
    };

    public void updateProgress() {
        handler.postDelayed(updateTime, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.postDelayed(updateTime, 100);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(updateTime);
        int totalDuration = audioPlayerService.getTotalTime();
        int currentPosition = Helpers.progressToTimer(seekBar.getProgress(),
                totalDuration);
        audioPlayerService.seek(currentPosition);
        updateProgress();
    }


    @Override
    public void onItemAddQueue(Track track) {
        if(audioPlayerService!=null) {
            audioPlayerService.addTrackToQueue(track);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
