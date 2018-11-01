package lrandomdev.com.online.mp3player.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.ToxicBakery.viewpager.transforms.StackTransformer;
import com.ToxicBakery.viewpager.transforms.TabletTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutTranformer;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.adapters.CustomFragmentPageAdapter;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.MyPlaylist;
import lrandomdev.com.online.mp3player.models.Track;

/**
 * Created by Lrandom on 3/29/18.
 */

public class FragmentLibrary extends FragmentParent{
    private static final String TAG = FragmentLibrary.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;
    CustomFragmentPageAdapter customFragmentPageAdapter;
    Events events;
    String[] titles;
    String query;

    public static final FragmentLibrary newInstance() {
        FragmentLibrary fragment = new FragmentLibrary();
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        titles=getResources().getStringArray(R.array.tabs_title);
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        LinearLayout adView = (LinearLayout) view.findViewById(R.id.adView);
        Helpers.loadAd(getContext(),adView);
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.view_pager);
        customFragmentPageAdapter=new CustomFragmentPageAdapter(getChildFragmentManager(),getActivity());
        viewPager.setAdapter(customFragmentPageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                getActivity().setTitle(titles[position]);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(query!=null && !query.equalsIgnoreCase("")) {
                    query="";
                    Bundle bundle = new Bundle();
                    bundle.putString("query", query);
                    putArguments(bundle);
                }
            }
        });

        return view;
    }

    public void putArguments(Bundle args){
        query = args.getString("query");
        Bundle bundle =  new Bundle();
        bundle.putString("query",query);
        switch (viewPager.getCurrentItem()){
            case 0:
                FragmentTrack fragmentTrack= (FragmentTrack) viewPager.getAdapter().instantiateItem(viewPager,viewPager.getCurrentItem());
                fragmentTrack.putArguments(bundle);
                break;
            case 1:
                FragmentCategories fragmentCategories=(FragmentCategories) viewPager.getAdapter().instantiateItem(viewPager,viewPager.getCurrentItem());
                fragmentCategories.putArguments(bundle);
                break;
            case 2:
                FragmentAlbum fragmentAlbum=(FragmentAlbum) viewPager.getAdapter().instantiateItem(viewPager,viewPager.getCurrentItem());
                fragmentAlbum.putArguments(bundle);
                break;
            case 3:
                FragmentArtist fragmentArtist=(FragmentArtist)viewPager.getAdapter().instantiateItem(viewPager,viewPager.getCurrentItem());
                fragmentArtist.putArguments(bundle);
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int swipeTabEffect = Integer.valueOf(prefs.getString("swipe_tab_effects", "1"));
        switch (swipeTabEffect) {
            case 0:
                viewPager.clearAnimation();
                break;
            case 1:
                viewPager.setPageTransformer(true,new ZoomOutPageTransformer());
                break;
            case 2:
                viewPager.setPageTransformer(true, new TabletTransformer());
                break;
            case 3:
                viewPager.setPageTransformer(true, new CubeOutTransformer());
                break;
            case 4:
                viewPager.setPageTransformer(true, new StackTransformer());
                break;
            case 5:
                viewPager.setPageTransformer(true, new ZoomInTransformer());
                break;
            case 6:
                viewPager.setPageTransformer(true, new ZoomOutTranformer());
                break;
            case 7:
                viewPager.setPageTransformer(true, new RotateUpTransformer());
                break;
            case 8:
                viewPager.setPageTransformer(true, new AccordionTransformer());
                break;
            case 9:
                viewPager.setPageTransformer(true, new AccordionTransformer());
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        events=(Events)context;
    }

    public void trackClickItem(ArrayList<Track> tracks, int position){
        events.clickItem(tracks,position);
    }

    public void trackAddToQueue(Track track){
        events.addQueue(track);
    }

    public interface Events{
        public void clickItem(ArrayList<Track> tracks, int position);
        public void addQueue(Track track);
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    public void updateTotalTrack(MyPlaylist playlist){
        FragmentMyPlaylist fragmentPlaylist=(FragmentMyPlaylist)viewPager.getAdapter().instantiateItem(viewPager,4);
        fragmentPlaylist.updateTotalTrack(playlist);
    }

}
