package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.varunest.sparkbutton.SparkButton;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.Artist;
import lrandomdev.com.online.mp3player.models.Track;

public class AdapterLocalTrack extends RecyclerView.Adapter<AdapterLocalTrack.TrackViewHolder>{
    private Context context;
    private ArrayList<Track> tracks;
    AdapterTrack.OnItemClickListener mItemClickListener;
    AdapterTrack.OnTrackClickCallback mTrackClickCallback;
    FragmentLibrary fragmentLibrary=null;
    int resources;
    BoxStore boxStore;

    public AdapterLocalTrack(Context context, ArrayList<Track> tracks, int resources){
        this.context=context;
        this.tracks=tracks;
        this.resources=resources;
        this.mTrackClickCallback=((AdapterTrack.OnTrackClickCallback)context);
        boxStore= MainApplication.getApp().getBoxStore();
    }

    public AdapterLocalTrack(Context context, ArrayList<Track> tracks, FragmentLibrary fragmentLibrary, int resources){
        this.context=context;
        this.tracks=tracks;
        this.resources=resources;
        this.fragmentLibrary=fragmentLibrary;
        boxStore= MainApplication.getApp().getBoxStore();
    }

    @Override
    public AdapterLocalTrack.TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources,parent,false);
        return new AdapterLocalTrack.TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AdapterLocalTrack.TrackViewHolder holder, int position) {
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
        holder.btnMenu.setTag(track);
        holder.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
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
            btnFavorites.setVisibility(View.GONE);
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

    public void setOnItemClickListener(final AdapterTrack.OnItemClickListener mItemClickListener) {
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

                    case R.id.item_share:
                        Helpers.shareAction(context, track);
                        break;

                    case R.id.item_delete:
                        try {
                            File file = new File(track.getRealPath());
                            file.delete();
                            tracks.remove(track);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        notifyDataSetChanged();
                        break;
                }
                return true;
            }
        });
        menu.inflate(R.menu.menu_track_local);
        menu.show();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public interface OnTrackClickCallback{
        public void onItemClickCallback(int position, ArrayList<Track> tracks);
        public void onItemAddQueue(Track track);
    }
}
