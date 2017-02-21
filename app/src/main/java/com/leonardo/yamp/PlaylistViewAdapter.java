package com.leonardo.yamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaylistViewAdapter extends ArrayAdapter<Song> {

    TextView tvCurrentSong;

    public PlaylistViewAdapter(Context context, ArrayList<Song> songs) {
        super(context, R.layout.playlist_listview_item, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.playlist_listview_item, parent, false);

        Song currentSong = getItem(position);

        tvCurrentSong = (TextView) view.findViewById(R.id.tv_song);
        tvCurrentSong.setText(currentSong.getArtist() + " - " + currentSong.getName());

        return view;
    }
}
