package lrandomdev.com.online.mp3player.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import lrandomdev.com.online.mp3player.MainApplication;
import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.fragments.FragmentLibrary;
import lrandomdev.com.online.mp3player.helpers.Helpers;
import lrandomdev.com.online.mp3player.helpers.RestClient;
import lrandomdev.com.online.mp3player.models.MyPlaylist;
import lrandomdev.com.online.mp3player.models.Playlist;
import lrandomdev.com.online.mp3player.models.Track;
import lrandomdev.com.online.mp3player.models.Track_;


public class AdapterMyPlaylist extends RecyclerView.Adapter<AdapterMyPlaylist.PlaylistViewHolder>{
    private Context context;
    private ArrayList<MyPlaylist> playlists;
    private int resources;
    AdapterPlaylist.OnItemClickListener mItemClickListener;
    FragmentLibrary fragmentLibrary;
    BoxStore boxStore;

    public AdapterMyPlaylist(Context context,
                             ArrayList<MyPlaylist> playlists,
                             int resources,
                             FragmentLibrary fragmentLibrary){
        this.context=context;
        this.playlists=playlists;
        this.resources=resources;
        this.fragmentLibrary=fragmentLibrary;
        boxStore= MainApplication.getApp().getBoxStore();
    }


    @NonNull
    @Override
    public AdapterMyPlaylist.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(this.resources,parent,false);
        return new AdapterMyPlaylist.PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        MyPlaylist playlist= playlists.get(position);
        holder.tvTitle.setText(playlist.getName());
        holder.tvInfo.setText(playlist.getTotal_track()+" tracks");
        Glide.with(context)
                .load(RestClient.BASE_URL+playlists.get(position).getThumb())
                .apply(new RequestOptions().placeholder(R.drawable.bg_two).error(R.drawable.bg_two))
                .into(holder.imgThumb);
        holder.btnMenu.setTag(playlists.get(position));
        holder.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });
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

    public void setOnItemClickListener(final AdapterPlaylist.OnItemClickListener mItemClickListener) {
        this.mItemClickListener=mItemClickListener;
    }

    public void showMenu(final View view) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                final MyPlaylist playList= (MyPlaylist) view.getTag();
                switch (id) {
                    case R.id.item_play:
                        final Box<Track> box = boxStore.boxFor(Track.class);
                        ArrayList<Track> tracks=new ArrayList<Track>(box.query().equal(Track_.playlistId,playList.getId()).build().find());
                        if(tracks.size()==0){
                            Toast ts= Toast.makeText(context,context.getString(R.string.do_not_have_any_song_to_play),Toast.LENGTH_SHORT);
                            ts.show();
                        }else {
                            fragmentLibrary.trackClickItem(tracks,0);
                        }
                        break;

                    case R.id.item_rename:
                        final EditText edtName = new EditText(context);
                        edtName.setText(playList.getName());
                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.rename))
                                .setView(edtName)
                                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String name = edtName.getText().toString();
                                        Box<MyPlaylist> boxPlaylist = boxStore.boxFor(MyPlaylist.class);
                                        MyPlaylist playlist= boxPlaylist.get(playList.getId());
                                        playlist.setName(name);
                                        boxPlaylist.put(playlist);
                                        playlists.get(playlists.indexOf(playList)).setName(name);
                                        notifyDataSetChanged();

                                    }
                                })
                                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                                .show();
                        break;

                    case R.id.item_remove:
                        Helpers.removePlaylist(boxStore,playList.getId());
                        playlists.remove(playList);
                        notifyDataSetChanged();
                        break;
                }
                return true;
            }
        });

        menu.inflate(R.menu.menu_playlist);
        menu.show();
    }
}

