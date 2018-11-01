package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.models.Artist;

/**
 * Created by Lrandom on 3/30/18.
 */

public class AdapterArtist extends RecyclerView.Adapter<AdapterArtist.ArtistViewHolder>{
    private Context context;
    private ArrayList<Artist> artists;
    AdapterArtist.OnItemClickListener mItemClickListener;
    FragmentLibrary fragmentLibrary;

    public AdapterArtist(Context context, ArrayList<Artist> artists,FragmentLibrary fragmentLibrary){
        this.context=context;
        this.artists=artists;
        this.fragmentLibrary=fragmentLibrary;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_artist_item,parent,false);
        return new ArtistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        holder.tvTitle.setText(artists.get(position).getArtist());
        Glide.with(context)
                .load(artists.get(position).getThumb())
                .apply(new RequestOptions().placeholder(R.drawable.ic_user).error(R.drawable.ic_user))
                .into(holder.imgThumb);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        CircleImageView imgThumb;

        public ArtistViewHolder(View itemView){
            super(itemView);
            imgThumb=(CircleImageView)itemView.findViewById(R.id.imgThumb);
            tvTitle=(TextView)itemView.findViewById(R.id.tvTitle);
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
