package lrandomdev.com.online.mp3player.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentSelectPlaylistDialog;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.ItemTouchHelperAdapter;
import lrandomdev.com.online.mp3player.helpers.ItemTouchHelperViewHolder;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.services.DownloadService;


/**
 * Created by Lrandom on 4/9/18.
 */

public class AdapterTrackInQueue extends RecyclerView.Adapter<AdapterTrackInQueue.TrackViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private ArrayList<Track> tracks;
    OnItemClickListener mItemClickListener;
    OnPlayListener mOnPlayListener;
    OnStartDragListener mOnStartDragListener;
    Track track;

    public AdapterTrackInQueue(Context context, ArrayList<Track> tracks){
        this.context=context;
        this.tracks=tracks;

    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_track_in_queue,parent,false);
        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TrackViewHolder holder, int position) {
        Track track =tracks.get(position);
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
        holder.btnMenu.setTag(tracks.get(position));
        holder.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });
        holder.btnDragDrop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mOnStartDragListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Track prev = tracks.remove(fromPosition);
        tracks.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        tracks.remove(position);
        notifyDataSetChanged();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,ItemTouchHelperViewHolder {
        TextView tvTitle;
        TextView tvArtist;
        ImageButton btnMenu;
        TextView tvDuration;
        ImageButton btnDragDrop;


        public TrackViewHolder(View itemView){
            super(itemView);
            tvTitle=(TextView)itemView.findViewById(R.id.tvTitle);
            tvArtist=(TextView)itemView.findViewById(R.id.tvArtist);
            tvDuration=(TextView)itemView.findViewById(R.id.tvTvDuration);
            btnMenu=(ImageButton)itemView.findViewById(R.id.btnMenu);
            btnDragDrop=(ImageButton)itemView.findViewById(R.id.btnDragDrop);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }


        @Override
        public void onItemSelected() {
            itemView.setBackgroundResource(R.color.colorGray);
        }

        @Override
        public void onItemClear() {
           itemView.setBackgroundResource(R.drawable.ripple);
        }
    }


    public void showMenu(final View view) {
        track = (Track)view.getTag();
        PopupMenu menu = new PopupMenu(context, view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.item_play:
                        mOnPlayListener.onItemClickCallback(tracks.indexOf(track),tracks);
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

                    case R.id.item_download:
                        Intent intent = new Intent(context, DownloadService.class);
                        intent.putExtra("file",track);
                        context.startService(intent);
                        //Toast.makeText(context,"This not avaiable in demo",Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.item_share:
                        if(track.getRemoteId()==null) {
                            Helpers.shareAction(context, track);
                        }else{
                            Helpers.shareAction(context, RestClient.BASE_URL+"detail?id="+track.getRemoteId());
                        }
                        break;
                }
                return true;
            }
        });

        if(track.getRemoteId()!=null) {
            menu.inflate(R.menu.menu_track_while_play);
        }else{
            menu.inflate(R.menu.menu_track_local_while_play);
        }
        menu.show();


        SharedPreferences prefs = context.getSharedPreferences("allow_download",Context.MODE_PRIVATE);
        int isAllow= prefs.getInt("is_allow",0);
        if(isAllow==0){
            menu.getMenu().findItem(R.id.item_download).setVisible(false);
        }else{
            menu.getMenu().findItem(R.id.item_download).setVisible(true);
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener=mItemClickListener;
    }

    public void setOnPlayListener(final OnPlayListener mOnPlayerListener) {
        this.mOnPlayListener=mOnPlayerListener;
    }

    public void setOnDragListener(final OnStartDragListener mOnStartDragListener) {
        this.mOnStartDragListener=mOnStartDragListener;
    }



    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public interface OnPlayListener{
        public void onItemClickCallback(int position, ArrayList<Track> tracks);
    }
    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

}