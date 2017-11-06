package com.example.shishir.mymusicplayer;


import java.io.Serializable;

public class Song {

    private long id;
    private String title;
    private String artist;
    private String duration;
    private long albumId;

    public Song(long songID, String songTitle, String songArtist, String duration, long albumId) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        this.duration = duration;
        this.albumId=albumId;
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public  long getAlbumId(){return albumId;}
}