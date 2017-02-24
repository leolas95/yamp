package com.leonardo.yamp;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MediaPlayer mediaPlayer;

    private static Song currentSong = null;

    private SeekBar seekbar;

    private final int MAX_PLAYLIST_LEN = 10;
    private ArrayList<Song> playList = new ArrayList<>(MAX_PLAYLIST_LEN);
    private int playlistIndex = -1;

    private final int PICK_SONG_REQUEST = 1;
    private final int ADD_SONG_TO_PLAYLIST_REQUEST = 2;

    Button playOrPauseButton;

    TextView tvArtist;
    TextView tvSong;

    ListView playlistListView;
    PlaylistViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        adapter = new PlaylistViewAdapter(this, playList);
        playlistListView = (ListView) findViewById(R.id.listvPlaylist);
        playlistListView.setAdapter(adapter);

        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song songToPlay = playList.get(position);
                playSelectedSong(songToPlay.getUri());
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mediaPlayer != null)
            mediaPlayer.start();

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
        if (mediaPlayer == null) {
            return;
        }

        // Was playing, pause it
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playOrPauseButton.setText(R.string.play);
        }
        // Was paused, play
        else {
            mediaPlayer.start();
            playOrPauseButton.setText(R.string.pause);
        }
    }

    public void stopMusic(View v) {
        if (mediaPlayer != null) {
            seekbar.setProgress(0);
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        tvArtist.setText("");
        tvSong.setText("");
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

        putSongDataOnView(songUri);
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
            mediaPlayer = MediaPlayer.create(this, playList.get(playlistIndex).getUri());
            mediaPlayer.setOnCompletionListener(listener);
            seekbar.setMax(mediaPlayer.getDuration());

            putSongDataOnView(playList.get(playlistIndex).getUri());

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

            mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex).getUri());
            mediaPlayer.prepare();
            seekbar.setMax(mediaPlayer.getDuration());
            seekbar.setProgress(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        putSongDataOnView(playList.get(playlistIndex).getUri());

        mediaPlayer.start();

    }

    /**
     * Advances to the next song in the playlist, checking that it doesn't go out the list limit
     * @param v NOT USED - Necessary for methods defined on the onClick property in xml of buttons
     */
    public void nextSongOnPlaylist(View v) {

        if (playList.isEmpty()) {
            Toast.makeText(this, "Empty playlist. Try adding a few songs first.", Toast.LENGTH_SHORT).show();
            return;
        }


        // If overflows, then just stop the music
        if (playlistIndex+1 >= playList.size()) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                seekbar.setProgress(0);
            }
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

            mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex).getUri());
            mediaPlayer.prepare();
            seekbar.setMax(mediaPlayer.getDuration());
            seekbar.setProgress(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        putSongDataOnView(playList.get(playlistIndex).getUri());

        mediaPlayer.start();
    }

    /**
     * Called when the user wants to add a song from the device to the playlist
     * @param v NOT USED. Necessary for the onClick methods declared on the activity's .xml file
     */
    public void addToPlaylist(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Log.d(TAG, "addToPlaylist() --> Starting activity for result");
        startActivityForResult(intent, ADD_SONG_TO_PLAYLIST_REQUEST);
    }

    private MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            Toast.makeText(MainActivity.this, "onCompletion", Toast.LENGTH_SHORT).show();

            // Check end of playlist
            if (playlistIndex+1 >= playList.size()) {
                seekbar.setProgress(0);
                return;
            } else {
                playlistIndex++;
            }

            // Go back to the Idle state, so we need to initialize it (set data source) and prepare
            // it
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(getApplicationContext(), playList.get(playlistIndex).getUri());
                mediaPlayer.prepare();
                seekbar.setMax(mediaPlayer.getDuration());
                seekbar.setProgress(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            putSongDataOnView(playList.get(playlistIndex).getUri());

            mediaPlayer.start();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PICK_SONG_REQUEST:
                if (resultCode == RESULT_OK) {

                    if (data == null) {
                        Log.e(TAG, "onActivityResult: Intent data = null");
                        return;
                    }

                    Uri songUri = data.getData();

                    if (songUri.toString().contains("%20")) {
                        Log.d(TAG, "songUri contains \'%20\'");
                        Log.d(TAG, "songUri before = " + songUri.toString());
                        songUri = Uri.parse(songUri.toString().replace("%20", " "));
                        Log.d(TAG, "songUri now = " + songUri.toString());
                    }

                    String mimeType = getMimeType(songUri);
                    Log.d(TAG, "getMimeType went OK");

                    if (mimeType == null) {
                        Log.e(TAG, "onActivityResult: mimetype null");
                        return;
                    }

                    // Check that the user effectively selected an audio file; we can't "play" and image.
                    if (!isAudioFile(mimeType)) {
                        Toast.makeText(this, "Error: You can only select audio files (mp3, mp3, mpeg, etc)"
                                + " --> " + mimeType, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "So far so good");
                    putSongDataOnView(songUri);

                    playSelectedSong(songUri);
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Operation canceled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "PICK_SONG_REQUEST: Error");
                    Toast.makeText(this, "result NOT ok", Toast.LENGTH_SHORT).show();
                }
                
                break;

            case ADD_SONG_TO_PLAYLIST_REQUEST:
                if (resultCode == RESULT_OK) {

                    if (data == null) {
                        Log.e(TAG, "onActivityResult: Intent data = null");
                        return;
                    }

                    Uri songUri = data.getData();

                    if (songUri.toString().contains("%20")) {
                        Log.d(TAG, "songUri contains \'%20\'");
                        Log.d(TAG, "songUri before = " + songUri.toString());
                        songUri = Uri.parse(songUri.toString().replace("%20", " "));
                        Log.d(TAG, "songUri now = " + songUri.toString());
                    }

                    String mimeType = getMimeType(songUri);
                    Log.d(TAG, "getMimeType went OK");

                    if (mimeType == null) {
                        Log.e(TAG, "onActivityResult: mimetype null");
                        return;
                    }

                    if (!isAudioFile(mimeType)) {
                        Toast.makeText(this, "Error: You can only select audio files (mp3, mp3, mpeg, etc)",
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "ADD_SONG_TO_PLAYLIST_REQUEST: mimetype = " + mimeType);
                        return;
                    }

                    Song newSong = obtainSongFromUriMetadata(songUri);
                    playList.add(newSong);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, newSong.getName() + " added to playlist", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "ADD_SONG_TO_PLAYLIST_REQUEST: Error");
                }
                break;
        }
    }

    /**
     * Obtains the mimetype of uri, returning it as a String
     * @param uri the Uri to obtain the mimetype from
     * @return a String containing the mimetype of the specified Uri
     */
    private String getMimeType(Uri uri) {
        String mimeType = null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            Log.d(TAG, "getMimeType: uri starts with content://");
            ContentResolver cr = getApplicationContext().getContentResolver();

            Log.d(TAG, "getMimeType: uri = " + uri.toString());
            mimeType = cr.getType(uri);

        } else {
            String fileExtension = getFileExtensionFromUri(uri);
            Log.d(TAG, "getMimeType: fileExt = " + fileExtension);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }


        Log.d(TAG, "Returning mimetype = " + mimeType);
        return mimeType;
    }

    /**
     * Obtains the file extension of a file from a given Uri, and returns it as a String without
     * the leading '.' (e.g: mp3, not .mp3)
     * @param uri the uri from which to obtain the file extension
     * @return the file extension as a String
     */
    private String getFileExtensionFromUri(Uri uri) {
        return uri.toString().substring(uri.toString().lastIndexOf(".") + 1);
    }

    /**
     * Checks if the string representing a mimetype for a file is for audio only
     * @param mimeType String representing the mimetype
     * @return true if the mimetype is audio, false otherwise
     */
    private boolean isAudioFile(String mimeType) {
        return (mimeType != null && mimeType.contains("audio"));
    }

    /**
     * Sets the current song to that from songUri, and uses its artist and song fields to fill the
     * respective TextViews.
     * @param songUri the uri from which obtain the current song
     */
    public void putSongDataOnView(Uri songUri) {

        Log.d(TAG, "calling obtainSongFromUriMetadata");
        currentSong = obtainSongFromUriMetadata(songUri);

        tvSong.setText(currentSong.getName());
        tvSong.setTextColor(Color.GREEN);

        tvArtist.setText(currentSong.getArtist());
        tvArtist.setTextColor(Color.GREEN);
    }

    /**
     * Takes the Uri of a song and retrieves its title and artist
     * @param songUri the Uri to take the data from
     * @return a Song with the title and artist fields taken from the songUri metadata
     */
    public Song obtainSongFromUriMetadata(Uri songUri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        Log.d(TAG, "obtainSongFromUriMetadata: songUri received: " + songUri);

        String absPath = new File(songUri.getPath()).getAbsolutePath();
        Log.d(TAG, "absPath = " + absPath);

        try {
            mmr.setDataSource(absPath);

            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            if (artist == null)
                artist = "<unknown artist>";
            if (title == null)
                title = "<unknown song>";

            return new Song(title, artist, songUri);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException on obtainSongFromUriMetadata");
            Log.e(TAG, "absPath = " + absPath);
            Log.e(TAG, "songUri = " + songUri);
            return new Song("<unknown song>", "<unknown artist>", songUri);
        }
    }
}