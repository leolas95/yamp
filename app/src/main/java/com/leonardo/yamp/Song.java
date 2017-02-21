package com.leonardo.yamp;

import android.net.Uri;

public class Song {
    private String name;
    private String artist;
    private Uri uri;

    public Song(String name, String artist, Uri uri) {
        this.name = name;
        this.artist = artist;
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public Uri getUri() {
        return uri;
    }
}
