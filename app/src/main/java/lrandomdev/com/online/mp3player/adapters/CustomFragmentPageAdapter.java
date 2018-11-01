package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentAlbum;
import lrandomdev.com.online.mp3player.fragments.FragmentArtist;
import lrandomdev.com.online.mp3player.fragments.FragmentCategories;
import lrandomdev.com.online.mp3player.fragments.FragmentDownload;
import lrandomdev.com.online.mp3player.fragments.FragmentMyPlaylist;
import lrandomdev.com.online.mp3player.fragments.FragmentParent;
import lrandomdev.com.online.mp3player.fragments.FragmentPlaylist;
import lrandomdev.com.online.mp3player.fragments.FragmentTrack;


/**
 * Created by Lrandom on 3/29/18.
 */

public class CustomFragmentPageAdapter extends FragmentPagerAdapter {
    private static final String TAG = CustomFragmentPageAdapter.class.getSimpleName();
    private static final int FRAGMENT_COUNT = 7;
    FragmentParent fragment;
    String[] titles;
    Context context;

    public CustomFragmentPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context=context;
        titles=this.context.getResources().getStringArray(R.array.tabs_title);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                fragment= FragmentTrack.newInstance();
                break;

            case 1:
                fragment= FragmentCategories.newInstance();
                break;
            case 2:
                fragment= FragmentAlbum.newInstance();
                break;

            case 3:
                fragment= FragmentArtist.newInstance();
                break;

            case 4:
                fragment= FragmentPlaylist.newInstance();
                break;

            case 5:
                fragment= FragmentDownload.newInstance();
                break;

            case 6:
                fragment= FragmentMyPlaylist.newInstance();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
