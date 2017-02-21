package com.leonardo.yamp;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private SeekBar seekbar;

    private final int MAX_PLAYLIST_LEN = 5;

    private ArrayList<Uri> playList = new ArrayList<>(MAX_PLAYLIST_LEN);

    private int playlistIndex = -1;

    private final int PICK_SONG_REQUEST = 1;
    private final int ADD_SONG_TO_PLAYLIST_REQUEST = 2;

    Button playOrPauseButton;

    TextView tvArtist;
    TextView tvSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        seekbar = (SeekBar) findViewById(R.id.seekbar);

        playOrPauseButton = (Button) findViewById(R.id.btn_play_pause);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvSong = (TextView) findViewById(R.id.tv_song);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // This is to prevent the seekbar to change when no song is playing
                if (mediaPlayer == null) {
                    seekBar.setProgress(0);
                    seekBar.setFocusable(false);
                    return;
                }

                if (fromUser)
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

        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();

        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();

        if (mediaPlayer != null)
            mediaPlayer.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    // Pauses or resumes the music
    public void pauseMusic(View v) {
        if (mediaPlayer == null) {
            return;
        }

        // Was playing, pause it
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playOrPauseButton.setText("Play");
        }
        // Was paused, play
        else {
            mediaPlayer.start();
            playOrPauseButton.setText("Pause");
        }
    }

    public void stopMusic(View v) {
        if (mediaPlayer != null) {
            seekbar.setProgress(0);
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    // Let the user select a song from the device
    public void searchSong(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_SONG_REQUEST);
    }

    /**
     * Called from the PICK_SONG_REQUEST in onActivityResult()
     * @param songUri the Uri of the song to play
     */
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
            seekbar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Handler handler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
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
            mediaPlayer.setOnCompletionListener(listener);
            seekbar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
        }

        final Handler handler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekbar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 10);
            }
        });
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
            seekbar.setMax(mediaPlayer.getDuration());
            seekbar.setProgress(0);
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
            seekbar.setMax(mediaPlayer.getDuration());
            seekbar.setProgress(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    /**
     * Called when the user wants to add a song from the device to the playlist
     * @param v NOT USED. Necessary for the onClick methods declared on the activity's .xml file
     */
    public void addToPlaylist(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, ADD_SONG_TO_PLAYLIST_REQUEST);
    }

    private MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            Toast.makeText(MainActivity.this, "onCompletion", Toast.LENGTH_SHORT).show();

            // Check end of playlist
            if (playlistIndex+1 >= playList.size()) {
                return;
            } else {
                playlistIndex++;
            }

            // Go back to the Idle state, so we need to initialize it (set data source) and prepare
            // it
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex));
                mediaPlayer.prepare();
                seekbar.setMax(mediaPlayer.getDuration());
                seekbar.setProgress(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
        }
    };

    /**
     * Obtains the mimetype of uri, returning it as a String
     * @param uri the Uri to obtain the mimetype from
     * @return a String containing the mimetype of the specified Uri
     */
    private String getMimeType(Uri uri) {
        String mimeType = null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }

        return mimeType;
    }

    /**
     * Checks if the string representing a mimetype for a file is for audio only
     * @param mimeType String representing the mimetype
     * @return true if the mimetype is audio, false otherwise
     */
    private boolean isAudioFile(String mimeType) {
        return (mimeType != null && mimeType.contains("audio"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PICK_SONG_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri songUri = data.getData();
                    String mimeType = getMimeType(songUri);

                    // Check that the user effectively selected an audio file; we can "play" and image.
                    if (!isAudioFile(mimeType)) {
                        Toast.makeText(this, "Error: You can only select audio files (mp3, mp3, mpeg, etc)",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(new File(songUri.getPath()).getAbsolutePath());

                    String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String song = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                    if (artist == null)
                        artist = "<unknown artist>";
                    if (song == null)
                        song = "<unknown song>";

                    tvArtist.setText(artist);
                    tvSong.setText(song);

                    Toast.makeText(this, artist + " - " + song, Toast.LENGTH_SHORT).show();


                    playSelectedSong(songUri);
                }
                break;

            case ADD_SONG_TO_PLAYLIST_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri songUri = data.getData();
                    String mimeType = getMimeType(songUri);

                    if (!isAudioFile(mimeType)) {
                        Toast.makeText(this, "Error: You can only select audio files (mp3, mp3, mpeg, etc)",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(new File(songUri.getPath()).getAbsolutePath());

                    String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String song = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                    if (artist == null)
                        artist = "<unknown artist>";
                    if (song == null)
                        song = "<unknown song>";

                    tvArtist.setText(artist);
                    tvSong.setText(song);


                    playList.add(songUri);
                    Toast.makeText(this, "Song added to playlist", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
