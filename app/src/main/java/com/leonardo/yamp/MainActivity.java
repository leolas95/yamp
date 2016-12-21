package com.leonardo.yamp;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private SeekBar seekbar;

    private final int MAX_PLAYLIST_LEN = 2;

    private ArrayList<Uri> playList = new ArrayList<>(MAX_PLAYLIST_LEN);

    private int playlistIndex = -1;

    private final int PICK_SONG_REQUEST = 1;
    private final int ADD_SONG_TO_PLAYLIST_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        seekbar = (SeekBar) findViewById(R.id.seekbar);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing
            }
        });
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

    // Pauses or resumes the music
    public void pauseMusic(View v) {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        else
            mediaPlayer.start();
    }

    public void stopMusic(View v) {
        if (mediaPlayer != null) {
            seekbar.setProgress(0);
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    // Let's the user select a song from the device
    public void searchSong(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_SONG_REQUEST);
    }

    public void playSelectedSong(Uri songUri) {

        // If the player is playing, then stop it.
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();

        // To change the data source we need to be in the idle state, so a reset is needed (or
        // a creation if the player does not exist).
        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            else
                mediaPlayer.reset();

            mediaPlayer.setDataSource(getApplicationContext(), songUri);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekbar.setMax(mediaPlayer.getDuration());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Handler handler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    Toast.makeText(MainActivity.this, "asd" + mediaPlayer.getCurrentPosition(), Toast.LENGTH_SHORT).show();
                    seekbar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 10);
            }
        });

        mediaPlayer.start();
    }

    public void startPlaylist(View v) {

        if (playList.isEmpty()) {
            Toast.makeText(this, "Empty playlist. Try adding a few songs first.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (playList.size() >= 1) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            playlistIndex = 0;
            mediaPlayer = MediaPlayer.create(this, playList.get(playlistIndex));
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(listener);
        }
    }

    public void previousSongOnPlaylist(View v) {

        if (playList.isEmpty()) {
            Toast.makeText(this, "Empty playlist. Try adding a few songs first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (playlistIndex-1 < 0) {
            if (mediaPlayer != null)
                mediaPlayer.stop();
            return;
        }

        playlistIndex--;

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            else
                mediaPlayer.reset();

            mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

    }

    public void nextSongOnPlaylist(View v) {

        if (playList.isEmpty()) {
            Toast.makeText(this, "Empty playlist. Try adding a few songs first.", Toast.LENGTH_SHORT).show();
            return;
        }


        // If overflows, then just stop the music
        if (playlistIndex+1 >= playList.size()) {
            if (mediaPlayer != null)
                mediaPlayer.stop();
            return;
        }

        // It is safe to advance
        playlistIndex++;

        // This would be for a "circular" playlist; when reached the end, go back to the start.
        //playlistIndex = (playlistIndex+1) % playList.size();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            else
                mediaPlayer.reset();

            mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    public void addToPlaylist(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, ADD_SONG_TO_PLAYLIST_REQUEST);
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
                    Toast.makeText(this, "Song added to playlist", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
