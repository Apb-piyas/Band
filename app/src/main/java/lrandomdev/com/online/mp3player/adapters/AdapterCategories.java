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
import lrandomdev.com.online.mp3player.models.Categories;

public class AdapterCategories extends RecyclerView.Adapter<AdapterCategories.CategoriesViewHolder>{
    private Context context;
    private ArrayList<Categories> categories;
    private int resources;
    AdapterCategories.OnItemClickListener mItemClickListener;
    FragmentLibrary fragmentLibrary;

    public AdapterCategories(Context context, ArrayList<Categories> categories, int resources, FragmentLibrary fragmentLibrary){
        this.context=context;
        this.categories=categories;
        this.resources=resources;
        this.fragmentLibrary=fragmentLibrary;
    }

    @Override
    public AdapterCategories.CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources,parent,false);
        return new AdapterCategories.CategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdapterCategories.CategoriesViewHolder holder, int position) {
        Categories categorie = categories.get(position);
        holder.tvTitle.setText(categorie.getTitle());
        Glide.with(context)
                .load(categorie.getThumb())
                .apply(new RequestOptions().placeholder(R.drawable.bg_two).error(R.drawable.bg_two))
                .into(holder.imgThumb);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvArtist;
        ImageView imgThumb;

        public CategoriesViewHolder(View itemView){
            super(itemView);
            tvTitle=(TextView)itemView.findViewById(R.id.tvTitle);
            tvArtist=(TextView)itemView.findViewById(R.id.tvArtist);
            imgThumb=(ImageView)itemView.findViewById(R.id.imgThumb);
            tvArtist.setVisibility(View.GONE);
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