package lrandomdev.com.online.mp3player.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import lrandomdev.com.online.mp3player.R;
import lrandomdev.com.online.mp3player.models.Track;


/**
 * Created by Lrandom on 4/7/18.
 */

public class AdapterTrackSelect extends ArrayAdapter<Track>{
    private Context context;
    private int itemLayoutResource;
    private ArrayList<Track> tracks;

    public AdapterTrackSelect(Context context, int itemLayoutResource,
                                 ArrayList<Track> items) {
        super(context, itemLayoutResource, items);
        this.itemLayoutResource = itemLayoutResource;
        this.context = context;
        this.tracks=items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(this.itemLayoutResource, null);
        }
        Track playlistItem = getItem(position);
        TextView title = (TextView) view.findViewById(R.id.tvTitle);
        title.setText(playlistItem.getTitle());
        return view;
    }
}
