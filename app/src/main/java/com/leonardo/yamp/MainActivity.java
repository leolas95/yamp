package com.leonardo.yamp;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private final int MAX_SONGS = 2;
    private final int MAX_PLAYLIST_LEN = 5;

    public int songs[] = new int[MAX_SONGS];
    private int i = 0;

    public ArrayList<Uri> playList = new ArrayList<>(MAX_PLAYLIST_LEN);
    private int playlistIndex = 0;

    private final int PICK_SONG_REQUEST = 1;
    private final int ADD_SONG_TO_PLAYLIST_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = null;
        songs[0] = R.raw.three_doors_down;
        songs[1] = R.raw.do_i_wanna_know;
    }

    public void playNextSong(View v) {
        if (mediaPlayer == null) {
            i = 0;
            mediaPlayer = MediaPlayer.create(this, songs[i]);
            mediaPlayer.start();
            i++;
            return;
        }

        if (i < MAX_SONGS) {
            mediaPlayer.stop();
            mediaPlayer = null;
            mediaPlayer = MediaPlayer.create(this, songs[i]);
            mediaPlayer.start();
            i++;
            return;
        }

        i = 0;
        mediaPlayer.stop();
        mediaPlayer = null;
        mediaPlayer = MediaPlayer.create(this, songs[i]);
        mediaPlayer.start();
        i++;
    }

    public void play3DoorsDown(View v) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.three_doors_down);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(this, R.raw.three_doors_down);
            mediaPlayer.start();

        }
        i++;
    }

    public void playDoIWannaKnow(View v) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.do_i_wanna_know);
            mediaPlayer.start();
        } else {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(this, R.raw.do_i_wanna_know);
            mediaPlayer.start();
        }
        i++;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mediaPlayer != null)
            mediaPlayer.start();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /* Pausa o resume el audio */
    public void pauseMusic(View v) {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }

    public void stopMusic(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            //mediaPlayer = null;
        }
    }

    /**
     * Let the user select a song on the device and play it
     *
     * @param v
     */
    public void searchSong(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_SONG_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PICK_SONG_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri songUri = data.getData();
                    playSelectedSong(songUri);
                }
                break;
            case ADD_SONG_TO_PLAYLIST_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri songUri = data.getData();
                    playList.add(songUri);
                }
                break;
        }
    }

    public void playSelectedSong(Uri songUri) {
        // If the player is playing, then stop it.
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), songUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    public void startPlaylist(View v) {

        if (playList.size() >= 1) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = MediaPlayer.create(this, playList.get(playlistIndex));
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(listener);
        }
    }

    public void addToPlaylist(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_SONG_REQUEST);
    }

    private MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            playlistIndex++;
            mediaPlayer.release();
            mediaPlayer = null;

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
        }
    };
}
