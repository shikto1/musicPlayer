package com.example.shishir.mymusicplayer.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shishir.mymusicplayer.R;
import com.example.shishir.mymusicplayer.Song;

import java.util.ArrayList;


public class SongAdapterForSearchList extends BaseAdapter implements Filterable {

    private ArrayList<Song> songList;
    private ArrayList<Song> orig;
    private Context context;
    private AppCompatActivity activity;
    private Intent intent;

    public SongAdapterForSearchList(Context context, ArrayList<Song> songList) {
        this.context = context;
        activity = (AppCompatActivity) context;
        intent = activity.getIntent();
        this.songList = songList;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Song> results = new ArrayList<Song>();
                if (orig == null)
                    orig = songList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final Song g : orig) {
                            if (g.getTitle().toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                songList = (ArrayList<Song>) results.values;
                notifyDataSetChanged();
            }
        };

    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView songTitleTv, artistTv;
        TextView songDuration;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.single_song, null);
            holder = new ViewHolder();
            holder.songTitleTv = (TextView) convertView.findViewById(R.id.song_title);
            holder.artistTv = (TextView) convertView.findViewById(R.id.artist);
            holder.songDuration = (TextView) convertView.findViewById(R.id.song_duration);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.songTitleTv.setText(songList.get(position).getTitle());
        holder.songDuration.setText(songList.get(position).getDuration());
        holder.artistTv.setText(songList.get(position).getArtist());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("songId", songList.get(position).getID());
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
                activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
        });

        return convertView;
    }
}
