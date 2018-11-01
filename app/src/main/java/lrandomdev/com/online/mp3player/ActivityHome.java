package lrandomdev.com.online.mp3player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.gson.JsonObject;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wang.avi.AVLoadingIndicatorView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import lrandomdev.com.online.mp3player.adapters.AdapterTrackInQueue;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.fragments.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.helpers.ApiServices;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.helpers.SimpleItemTouchHelperCallback;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.ServicePlayer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lrandom on 3/23/18.
 */

public class ActivityHome extends ActivityParent implements
        SearchView.OnQueryTextListener,
        FragmentLibrary.Events,
        SeekBar.OnSeekBarChangeListener {
    private static final String TAG = ActivityHome.class.getSimpleName();
    private FragmentManager fragmentManager;
    private FragmentParent fragment = null;
    private SearchView mSearchView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    LinearLayout miniPlayback;
    SlidingUpPanelLayout.PanelState oldPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
    ServicePlayer audioPlayerService;
    Handler handler = new Handler();
    ArrayList<Track> tracks = new ArrayList<Track>();
    TextView tvTitle, tvTotalTime, tvElapsedTime, tvArtist, tvMiniTitle, tvMiniArtist, tvProfile;
    ImageButton btnPlayAndPause, btnNext, btnPrev, btnRepeat, btnShuffle,
            btnPlaylist, btnShare, btnLyrics, btnAddToPlaylist, btnMiniPrev, btnMiniNext, btnMiniPlayAndPause, btnPlayOnMenu;
    SeekBar prgTrack;
    boolean isRepeat, isShuffle;
    int trackIndex;
    String thumb;
    CircleImageView imgThumb, imgProfile;
    FrameLayout frameLayout, frameProfile;
    ImageView imgBg;
    ImageButton btnBack;
    Animation rotation, rotationMenuDisk;
    ImageView imgMiniThumb, navigationHeaderBg;
    AdapterTrackInQueue adapterTrackInQueue;
    Boolean flagSetting = false;
    private ItemTouchHelper mItemTouchHelper;
    Intent intent;
    String lang;
    ApiServices apiService;
    String lyrics="";
    AVLoadingIndicatorView indicator,mainIndicator,navIndicator;

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ServicePlayer.UPDATE_UI:
                    updatePlaybackUI();
                    indicator.setVisibility(View.GONE);
                    mainIndicator.setVisibility(View.GONE);
                    navIndicator.setVisibility(View.GONE);

                    btnMiniPlayAndPause.setVisibility(View.VISIBLE);
                    btnPlayAndPause.setVisibility(View.VISIBLE);
                    btnPlayOnMenu.setVisibility(View.VISIBLE);
                    break;

                case ServicePlayer.BUFFERING:
                    indicator.setVisibility(View.VISIBLE);
                    mainIndicator.setVisibility(View.VISIBLE);
                    navIndicator.setVisibility(View.VISIBLE);

                    imgMiniThumb.setImageResource(R.drawable.bg);
                    imgThumb.setImageResource(R.drawable.bg);
                    imgThumb.refreshDrawableState();
                    imgProfile.setImageResource(R.drawable.bg);

                    Glide.with(getApplicationContext()).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                    Glide.with(getApplicationContext()).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(navigationHeaderBg);


                    tvMiniTitle.setText(getString(R.string.loading));
                    tvMiniArtist.setText(getString(R.string.loading));
                    tvArtist.setText(getString(R.string.loading));
                    tvArtist.setText(getString(R.string.loading));

                    btnMiniPlayAndPause.setVisibility(View.INVISIBLE);
                    btnPlayAndPause.setVisibility(View.INVISIBLE);
                    btnPlayOnMenu.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RestClient.getApiService();
        retrofit2.Call<JsonObject> call = apiService.getAds();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject jsonObject= response.body();
                MobileAds.initialize(getApplicationContext(), jsonObject.get("app_ad_id").getAsString());
                SharedPreferences.Editor editor =getSharedPreferences("ads",MODE_PRIVATE).edit();
                editor.putString("app_ad_id",jsonObject.get("app_ad_id").getAsString());
                editor.putString("banner_ad_unit", jsonObject.get("banner_ad_unit").getAsString());
                editor.putString("in_ad_unit", jsonObject.get("in_ad_unit").getAsString());
                editor.apply();

                SharedPreferences.Editor editor2 = getSharedPreferences("allow_download",MODE_PRIVATE).edit();
                editor2.putInt("is_allow",jsonObject.get("allow_download").getAsInt());
                editor2.apply();
            }

            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                SharedPreferences.Editor editor =getSharedPreferences("ads",MODE_PRIVATE).edit();
                editor.putString("app_ad_id","");
                editor.putString("banner_ad_unit", "");
                editor.putString("in_ad_unit", "");
                editor.apply();

                SharedPreferences.Editor editor2 = getSharedPreferences("allow_download",MODE_PRIVATE).edit();
                editor2.putInt("is_allow",0);
                editor2.apply();
            }
        });


        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .session(10)
                .ratingBarColor(R.color.colorPrimary).build();
        ratingDialog.show();

        Intent intent = new Intent(this,
                ServicePlayer.class);
        getApplicationContext().bindService(intent, serviceConnection,
                Context.BIND_AUTO_CREATE);

        SharedPreferences prefs = getSharedPreferences("timer_sleep", MODE_PRIVATE);
        if (!prefs.contains("h")) {
            SharedPreferences.Editor editor = getSharedPreferences("timer_sleep", MODE_PRIVATE).edit();
            editor.putInt("h", 0);
            editor.putInt("m", 0);
            editor.apply();
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        lang = prefs.getString("language", "");

        presetMainUI();
        presetPlaybackUI();
        if (audioPlayerService != null) {
            updatePlaybackUI();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.item_search));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search));
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Bundle bundle = new Bundle();
        bundle.putString("query", newText);
        fragment.putArguments(bundle);
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServicePlayer.UPDATE_UI);
        intentFilter.addAction(ServicePlayer.BUFFERING);
        registerReceiver(notificationReceiver, intentFilter);

        if (flagSetting) {
            int somePrefValue = Integer.valueOf(prefs.getString("themes", "0"));
            switch (somePrefValue) {
                case 0:
                    setTheme(R.style.PurpeTheme);
                    break;

                case 1:
                    setTheme(R.style.OrangeTheme);
                    break;
            }

            String language = prefs.getString("language", "en");
            if (!lang.equalsIgnoreCase(language)) {
                Locale locale = new Locale(language);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

            presetMainUI();
            presetPlaybackUI();
            if (audioPlayerService != null) {
                updatePlaybackUI();
            }

            flagSetting = false;
        } else {
            if (audioPlayerService != null) {
                updatePlaybackUI();
            }
        }

        if (getIntent().hasExtra("DOWNLOAD")) {
            //move to download fragment
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
    }

    void presetMainUI() {
        setContentView(R.layout.activity_home);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = FragmentLibrary.newInstance();

        fragmentTransaction.replace(R.id.main_container_wrapper, fragment);
        fragmentTransaction.commit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_music);
        frameProfile = (FrameLayout) navHeader.findViewById(R.id.frameProfile);
        navigationHeaderBg = (ImageView) navHeader.findViewById(R.id.navigation_header_bg);
        navIndicator=(AVLoadingIndicatorView)navHeader.findViewById(R.id.navIndicator);
        tvProfile = (TextView) navHeader.findViewById(R.id.tvProfile);
        imgProfile = (CircleImageView) navHeader.findViewById(R.id.imgProfile);
        btnPlayOnMenu = (ImageButton) navHeader.findViewById(R.id.btnPlayOnMenu);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_library:
                        fragment = FragmentLibrary.newInstance();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.main_container_wrapper, fragment);
                        transaction.commit();
                        break;

                    case R.id.nav_favorites:
                        intent = new Intent(ActivityHome.this, ActivityFavorites.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_privacy_policy:
                        Intent intent = new Intent(ActivityHome.this, ActivityPrivacyPolicy.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_about:
                        intent = new Intent(ActivityHome.this, ActivityAboutUs.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_sleep_timer:
                        SharedPreferences prefs = getSharedPreferences("timer_sleep", MODE_PRIVATE);
                        final int hour = prefs.getInt("h", 0);
                        final int minute = prefs.getInt("m", 0);
                        intent = new Intent();
                        intent.setAction(ServicePlayer.ALARM_PAUSE);

                        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        final PendingIntent pendingIntent = PendingIntent.getBroadcast(ActivityHome.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        final TimePickerDialog mTimePicker = new TimePickerDialog(ActivityHome.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                final Calendar mcurrentTime = Calendar.getInstance();
                                mcurrentTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                mcurrentTime.set(Calendar.MINUTE, minute);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis(), pendingIntent);
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis(), pendingIntent);
                                } else {
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, mcurrentTime.getTimeInMillis(), pendingIntent);
                                }

                                SharedPreferences prefs = getSharedPreferences("timer_sleep", MODE_PRIVATE);
                                if (prefs.contains("h")) {
                                    SharedPreferences.Editor editor = getSharedPreferences("timer_sleep", MODE_PRIVATE).edit();
                                    editor.putInt("h", hourOfDay);
                                    editor.putInt("m", minute);
                                    editor.apply();
                                }

                                Toast toast = Toast.makeText(ActivityHome.this, getString(R.string.player_stop_at) + " " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }, hour, minute, true);

                        mTimePicker.setButton(TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.reset), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = getSharedPreferences("timer_sleep", MODE_PRIVATE).edit();
                                editor.putInt("h", 0);
                                editor.putInt("m", 0);
                                editor.apply();
                                alarmManager.cancel(pendingIntent);
                                Toast toast = Toast.makeText(ActivityHome.this, getString(R.string.cancel_sleep_timer), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                        mTimePicker.show();
                        break;

                    case R.id.nav_equalizer:
                        intent = new Intent(ActivityHome.this, ActivityEqualizer.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_settings:
                        intent = new Intent(ActivityHome.this, ActivitySettings.class);
                        flagSetting = true;
                        startActivity(intent);
                        break;

                    case R.id.nav_share_app:
                        Helpers.shareApp(ActivityHome.this);
                        break;


                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                assert drawer != null;
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        miniPlayback = (LinearLayout) findViewById(R.id.miniPlayback);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + oldPanelState);
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
                Log.i(TAG, slidingUpPanelLayout.getPanelState() + "");

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
            // audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            audioPlayerService = ((ServicePlayer.PlayerBinder) service)
                    .getService();
            if (audioPlayerService.getTrackIndex() != -1) {
                updatePlaybackUI();
            }
        }
    };


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


        rotation = AnimationUtils.loadAnimation(ActivityHome.this, R.anim.rotation);
        rotation.setFillEnabled(true);
        rotation.setFillAfter(true);

        rotationMenuDisk = AnimationUtils.loadAnimation(ActivityHome.this, R.anim.rotation);
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
                if (tracks.get(trackIndex).getRemoteId() == null) {
                    Helpers.shareAction(ActivityHome.this, tracks.get(trackIndex));
                } else {
                    Helpers.shareAction(ActivityHome.this, RestClient.BASE_URL + "detail?id=" + tracks.get(trackIndex).getRemoteId());
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
                newFragment.show(ActivityHome.this.getFragmentManager(), "dialog");
            }
        });


        btnLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogPlus dialog = DialogPlus.newDialog(ActivityHome.this)
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
                    btnPlayOnMenu.setImageResource(R.drawable.ic_play);
                } else {
                    audioPlayerService.resume();
                    btnMiniPlayAndPause
                            .setImageResource(R.drawable.ic_stop_no_circle);
                    btnPlayAndPause
                            .setImageResource(R.drawable.ic_stop);
                    btnPlayOnMenu.setImageResource(R.drawable.ic_stop);
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

        btnPlayOnMenu.setOnClickListener(new View.OnClickListener() {
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
        tracks = audioPlayerService.getTracks();
        if (tracks != null) {
            trackIndex = audioPlayerService.getTrackIndex();
            Track track = tracks.get(trackIndex);

            tvProfile.setText(track.getTitle());
            tvTitle.setText(track.getTitle());

            String artist_text = "";
            ArrayList<Artist> artists = track.getArtists();
            if (artists != null && artists.size() != 0) {
                for (int i = 0; i < artists.size(); i++) {
                    if (i == (artists.size() - 1)) {
                        artist_text += artists.get(i).getArtist();
                    } else {
                        artist_text += artists.get(i).getArtist() + " , ";
                    }
                }
                tvArtist.setText(Helpers.trimRightComma(artist_text));
                tvMiniArtist.setText(Helpers.trimRightComma(artist_text));
            } else {
                tvArtist.setText(track.getArtist());
                tvMiniArtist.setText(track.getArtist());
            }

            if (audioPlayerService.isPlay()) {
                btnMiniPlayAndPause.setImageResource(R.drawable.ic_stop_no_circle);
                btnPlayAndPause.setImageResource(R.drawable.ic_stop);
                btnPlayOnMenu.setImageResource(R.drawable.ic_stop);
                frameLayout.startAnimation(rotation);
                frameProfile.startAnimation(rotationMenuDisk);

            } else {
                btnMiniPlayAndPause.setImageResource(R.drawable.ic_play_no_circle);
                btnPlayAndPause.setImageResource(R.drawable.ic_play);
                btnPlayOnMenu.setImageResource(R.drawable.ic_play);
                frameLayout.clearAnimation();
                frameProfile.clearAnimation();
            }

            tvMiniTitle.setText(track.getTitle());
            if (track.getThumb() != null) {
                thumb = tracks.get(trackIndex).getThumb();
                thumb = RestClient.BASE_URL + thumb;
                if (thumb != null) {
                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgMiniThumb);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgThumb);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.errorOf(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgProfile);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                    Glide.with(this).load(thumb)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(navigationHeaderBg);
                } else {
                    imgMiniThumb.setImageResource(R.drawable.bg);
                    imgThumb.setImageResource(R.drawable.bg);
                    imgThumb.refreshDrawableState();
                    imgProfile.setImageResource(R.drawable.bg);

                    Glide.with(this).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(imgBg);

                    Glide.with(this).load(R.drawable.bg)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(30, 15)).error(R.drawable.bg).placeholder(R.drawable.bg))
                            .into(navigationHeaderBg);

                }
            }

            adapterTrackInQueue = new AdapterTrackInQueue(ActivityHome.this, tracks);
            btnPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogPlus dialog = DialogPlus.newDialog(ActivityHome.this)
                            .setContentHolder(new ViewHolder(R.layout.layout_queue))
                            .setContentBackgroundResource(R.drawable.border_white)
                            .setExpanded(true, 800)
                            .create();

                    RecyclerView listQueue = (RecyclerView) dialog.getHolderView();
                    listQueue.setAdapter(adapterTrackInQueue);
                    listQueue.setLayoutManager(new LinearLayoutManager(ActivityHome.this));
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
            // TODO Auto-generated method stub
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

    //event click items
    @Override
    public void clickItem(ArrayList<Track> tracks, int position) {
        trackIndex = position;
        this.tracks = tracks;
        audioPlayerService.play(trackIndex, tracks);
    }

    @Override
    public void addQueue(Track track) {
        tracks.add(track);
        if (adapterTrackInQueue != null) {
            adapterTrackInQueue.notifyDataSetChanged();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            ViewPager viewPager = (ViewPager) fragment.getActivity().findViewById(R.id.view_pager);
            if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } else {
                finish();
            }
        }
    }
}
