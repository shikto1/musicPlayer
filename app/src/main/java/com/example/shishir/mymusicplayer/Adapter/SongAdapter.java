package com.example.shishir.mymusicplayer.Adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.shishir.mymusicplayer.R;
import com.example.shishir.mymusicplayer.Song;

import java.util.ArrayList;


public class SongAdapter extends BaseAdapter{

    private ArrayList<Song> songList;
    private ArrayList<Song> orig;
    private Context context;

    public SongAdapter(Context context, ArrayList<Song> songList) {
        this.context = context;
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

    static class ViewHolder {
        TextView songTitleTv, artistTv;
        TextView songDuration;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.single_song, null);
            holder = new ViewHolder();
            holder.songTitleTv = (TextView) convertView.findViewById(R.id.song_title);
            holder.artistTv=(TextView)convertView.findViewById(R.id.artist);
            holder.songDuration = (TextView) convertView.findViewById(R.id.song_duration);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.songTitleTv.setText(songList.get(position).getTitle());
        holder.songDuration.setText(songList.get(position).getDuration());
        holder.artistTv.setText(songList.get(position).getArtist());

        return convertView;
    }
}
