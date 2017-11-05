package com.example.shishir.mymusicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class AllSong {

    private ArrayList<Song> songList = new ArrayList<Song>();


    public ArrayList<Song> getSongList(Context context) {
        //query external audio
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int durationColmn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String duration = formatToRealTime(musicCursor.getLong(durationColmn));
                String albumId=musicCursor.getString(pathColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, duration, albumId));
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        return songList;
    }


    public static String formatToRealTime(long mills) {
        Date date = new Date(mills);
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
}
