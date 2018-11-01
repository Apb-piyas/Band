package lrandomdev.com.online.mp3player.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.varunest.sparkbutton.SparkButton;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.fragments.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.DownloadService;


/**
 * Created by Lrandom on 3/29/18.
 */

public class AdapterTrack extends RecyclerView.Adapter<AdapterTrack.TrackViewHolder>{
    private Context context;
    private ArrayList<Track> tracks;
    OnItemClickListener mItemClickListener;
    OnTrackClickCallback mTrackClickCallback;
    FragmentLibrary fragmentLibrary=null;
    int resources;
    BoxStore boxStore;

    public AdapterTrack(Context context, ArrayList<Track> tracks, int resources){
        this.context=context;
        this.tracks=tracks;
        this.resources=resources;
        this.mTrackClickCallback=((OnTrackClickCallback)context);
        boxStore= MainApplication.getApp().getBoxStore();
    }

    public AdapterTrack(Context context, ArrayList<Track> tracks, FragmentLibrary fragmentLibrary, int resources){
        this.context=context;
        this.tracks=tracks;
        this.resources=resources;
        this.fragmentLibrary=fragmentLibrary;
        boxStore= MainApplication.getApp().getBoxStore();
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources,parent,false);
        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TrackViewHolder holder, int position) {
        final Track track = tracks.get(position);
       holder.tvTitle.setText(track.getTitle());
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
           holder.tvArtist.setText(Helpers.trimRightComma(artist_text));
       }else {
           holder.tvArtist.setText(track.getArtist());
       }
       holder.tvDuration.setText(track.getDuration());

        boolean isFavorites= Helpers.checkFavoriest(boxStore,track.getRemoteId());
        track.setFavoriest(isFavorites);

        if(isFavorites){
            holder.btnFavorites.setChecked(true);
        }else{
            holder.btnFavorites.setChecked(false);
        }

        holder.btnFavorites.setTag(track);
        holder.btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Track track1 =(Track) v.getTag();
                Boolean isFavorites= Helpers.checkFavoriest(boxStore,track1.getRemoteId());
                if(isFavorites){
                    Helpers.removeFavorites(boxStore,track1.getRemoteId());
                    holder.btnFavorites.setChecked(false);
                    holder.btnFavorites.playAnimation();
                }else{
                    Helpers.addToFavoriest(boxStore,track1);
                    holder.btnFavorites.setChecked(true);
                    holder.btnFavorites.playAnimation();
                }
            }
        });


        int placeholder = R.drawable.ic_play;
        if(resources==R.layout.row_track_item_grid){
            placeholder=R.drawable.bg_two;
        }

        Glide.with(context)
                .load(RestClient.BASE_URL+track.getThumb())
                .apply(new RequestOptions().placeholder(placeholder).error(placeholder))
                .into(holder.imgThumb);

       holder.btnMenu.setTag(track);
       holder.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvArtist;
        ImageButton btnMenu;
        TextView tvDuration;
        ImageView imgThumb;
        SparkButton btnFavorites;

        public TrackViewHolder(View itemView){
            super(itemView);
            tvTitle=(TextView)itemView.findViewById(R.id.tvTitle);
            tvArtist=(TextView)itemView.findViewById(R.id.tvArtist);
            tvDuration=(TextView)itemView.findViewById(R.id.tvDuration);
            btnMenu=(ImageButton)itemView.findViewById(R.id.btnMenu);
            btnFavorites=(SparkButton) itemView.findViewById(R.id.btnFavorites);

            if(resources==R.layout.row_track_item){
                imgThumb=(CircleImageView)itemView.findViewById(R.id.imgThumb);
            }else{
                imgThumb=(ImageView)itemView.findViewById(R.id.imgThumb);
            }
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener=mItemClickListener;
    }

    public void showMenu(final View view) {
        PopupMenu menu = new PopupMenu(context, view);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                final Track track = (Track) view.getTag();
                switch (id) {
                    case R.id.item_play:
                        if (fragmentLibrary!=null) {
                            fragmentLibrary.trackClickItem(tracks, tracks.indexOf(track));
                        }else{
                            mTrackClickCallback.onItemClickCallback(tracks.indexOf(track),tracks);
                        }
                        break;

                    case R.id.item_add_to_queue:
                        if (fragmentLibrary!=null) {
                            fragmentLibrary.trackAddToQueue(track);
                        }else{
                            mTrackClickCallback.onItemAddQueue(track);
                        }
                        break;

                    case R.id.item_download:
                        Intent intent = new Intent(context, DownloadService.class);
                        intent.putExtra("file",track);
                        context.startService(intent);
                        //Toast.makeText(getContext(),"This not avaiable in demo",Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.item_to_playlist:
                        FragmentSelectPlaylistDialog newFragment = FragmentSelectPlaylistDialog
                                .newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("item",track);
                        newFragment.setArguments(bundle);
                        Activity activity=(Activity)context;
                        newFragment.show(activity.getFragmentManager(), "dialog");
                        break;

                    case R.id.item_share:
                        Helpers.shareAction(context, RestClient.BASE_URL+"detail?id="+track.getRemoteId());
                        break;
                }
                return true;
            }
        });

        menu.inflate(R.menu.menu_track);
        menu.show();

        SharedPreferences prefs = getContext().getSharedPreferences("allow_download",Context.MODE_PRIVATE);
        int isAllow= prefs.getInt("is_allow",0);
        if(isAllow==0){
            menu.getMenu().findItem(R.id.item_download).setVisible(false);
        }else{
            menu.getMenu().findItem(R.id.item_download).setVisible(true);
        }
    }


    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public interface OnTrackClickCallback{
        public void onItemClickCallback(int position, ArrayList<Track> tracks);
        public void onItemAddQueue(Track track);
    }


    public Context getContext() {
        return context;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}