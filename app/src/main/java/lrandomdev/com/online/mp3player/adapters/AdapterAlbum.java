package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.models.Album;
import lrandomdev.com.online.mp3player.models.Artist;

/**
 * Created by Lrandom on 3/30/18.
 */

public class AdapterAlbum extends RecyclerView.Adapter<AdapterAlbum.AlbumViewHolder>{
    private Context context;
    private ArrayList<Album> albums;
    private int resources;
    OnItemClickListener mItemClickListener;
    FragmentLibrary fragmentLibrary;

    public AdapterAlbum(Context context, ArrayList<Album> albums, int resources, FragmentLibrary fragmentLibrary){
        this.context=context;
        this.albums=albums;
        this.resources=resources;
        this.fragmentLibrary=fragmentLibrary;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources,parent,false);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.tvTitle.setText(album.getTitle());
        String artist_text = "";
        ArrayList<Artist> artists = albums.get(position).getArtists();

        if(artists!=null &&  artists.size()!=0) {
            for (int i = 0; i < artists.size(); i++) {
                if(i == (artists.size()-1)){
                    artist_text += artists.get(i).getArtist();
                }else {
                    artist_text += artists.get(i).getArtist() + " , ";
                }
            }
            holder.tvArtist.setText(Helpers.trimRightComma(artist_text));
        }else{
            holder.tvArtist.setText("");
        }

        Glide.with(context)
                .load(album.getThumb())
                .apply(new RequestOptions().placeholder(R.drawable.bg_two).error(R.drawable.bg_two))
                .into(holder.imgThumb);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvArtist;
        ImageView imgThumb;

        public AlbumViewHolder(View itemView){
            super(itemView);
            tvTitle=(TextView)itemView.findViewById(R.id.tvTitle);
            tvArtist=(TextView)itemView.findViewById(R.id.tvArtist);
            imgThumb=(ImageView)itemView.findViewById(R.id.imgThumb);
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