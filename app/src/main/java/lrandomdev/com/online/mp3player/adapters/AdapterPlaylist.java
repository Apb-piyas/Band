package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.models.Playlist;


/**
 * Created by Lrandom on 3/30/18.
 */

public class AdapterPlaylist extends RecyclerView.Adapter<AdapterPlaylist.PlaylistViewHolder>{
    private Context context;
    private ArrayList<Playlist> playlists;
    private int resources;
    OnItemClickListener mItemClickListener;
    FragmentLibrary fragmentLibrary;
    BoxStore boxStore;

    public AdapterPlaylist(Context context, ArrayList<Playlist> playlists, int resources, FragmentLibrary fragmentLibrary){
        this.context=context;
        this.playlists=playlists;
        this.resources=resources;
        this.fragmentLibrary=fragmentLibrary;
        boxStore= MainApplication.getApp().getBoxStore();
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources,parent,false);
        return new PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
        holder.tvTitle.setText(playlists.get(position).getName());
        holder.tvInfo.setText(playlists.get(position).getTotal_track()+" tracks");
        Glide.with(context)
                .load(playlists.get(position).getThumb())
                .apply(new RequestOptions().placeholder(R.drawable.bg_two).error(R.drawable.bg_two))
                .into(holder.imgThumb);

    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvInfo;
        ImageView imgThumb;
        ImageButton btnMenu;


        public PlaylistViewHolder(View itemView){
            super(itemView);
            tvTitle=(TextView)itemView.findViewById(R.id.tvTitle);
            tvInfo=(TextView)itemView.findViewById(R.id.tvInfo);
            imgThumb=(ImageView)itemView.findViewById(R.id.imgThumb);
            btnMenu=(ImageButton)itemView.findViewById(R.id.btnMenu);
            btnMenu.setVisibility(View.GONE);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener=mItemClickListener;
    }

}
