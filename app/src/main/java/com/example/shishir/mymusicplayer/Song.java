package com.example.shishir.mymusicplayer;


public class Song {

    private long id;
    private String title;
    private String artist;
    private String duration;
    private String albumId;

    public Song(long songID, String songTitle, String songArtist, String duration, String albumId) {
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

    public  String getImagePath(){return albumId;}
}