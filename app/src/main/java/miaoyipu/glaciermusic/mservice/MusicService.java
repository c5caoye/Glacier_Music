package miaoyipu.glaciermusic.mservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import miaoyipu.glaciermusic.FullScreenActivity;
import miaoyipu.glaciermusic.R;
import miaoyipu.glaciermusic.songs.Songs;

/**
 * Created by cy804 on 2017-06-05.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    private MediaSession mediaSession;

    private final IBinder mBind = new MusicBinder();
    private static final String TAG = "MService";
    private static final int NOTIFY_ID = 13;
    private static MediaPlayer player;

    private ArrayList<Songs> songList, shuffleList;
    private int songListSize, currentVolume, curPosn;
    private String songTitle = "Glacier Music", songArtist = "Unknown";
    private AudioManager audioManager;
    private boolean shuffle = false;
    private Uri songUri;
    private boolean isRunning = false;

    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Initializing MediaPlayer");
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnInfoListener(this);
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {return MusicService.this;}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBind; // mBind = new MusicBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.reset();
        player.release();
        player = null;
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN : // Resume the stream volume
                Log.d(TAG, "AUDIOFOCUS_GAIN");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "AUDIOFOCUS_LOSS");
                this.pause(); break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                this.pause(); break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // Lower volume
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int newVolume = new Double(maxVolume * 0.1).intValue();
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            default: break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {}

    public void setSongList(ArrayList<Songs> theSongs) {
        Log.d(TAG, "Setting song list");
        songList = theSongs;
        shuffleList = (ArrayList<Songs>) songList.clone();
        Collections.shuffle(shuffleList);
        songListSize = theSongs.size();
        curPosn = 0;
    }

    public void setShuffle() {
        Log.d(TAG, "Toggle Shuffle");
        shuffle = !shuffle;
    }

    public void pause() {
        Log.d(TAG, "PAUSE");
        player.pause();
    }

    public void pausePlay() {
        if (isRunning) {
            player.start();
        } else {
            play();
        }
    }

    public void play() {
        player.reset();
        Songs song;

        if (shuffle) {
            song = shuffleList.get(curPosn);
        } else {
            song = songList.get(curPosn);
        }

        startPlay(song);
    }

    public void play(int idx) {
        player.reset();
        Songs song = songList.get(idx);
        startPlay(song);
    }

    public void startPlay(Songs song) {
        songTitle = song.getTitle();
        songArtist = song.getArtist();
        songUri = song.getAlbumUri();

        long id = song.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e(TAG, "Error setting data source");
        }

        player.prepareAsync();
        isRunning = true;
    }

    public void playNext() {
        curPosn++;
        if (curPosn >= songListSize) {
            curPosn = 0;
        }
        play();
    }

    public void playPrev() {
        curPosn--;
        if (curPosn < 0) {
            curPosn = songListSize - 1;
        }
        play();
    }

    public void setSongAndPlay(int idx) {
        curPosn = idx;
        play(idx);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean isRunning() { return this.isRunning; }

    public boolean isInitialized() { return player != null; }

    public boolean isShuffle() {
        return this.shuffle;
    }

    public String getTitle() {
        return songTitle;
    }

    public String getArtist() { return songArtist; }

    public Uri getAlbumUri() { return songUri; }

    public MediaPlayer getPlayer() {
        return player;
    }

    public void seekTo(int progress) { player.seekTo(progress); }

    public int getDuration() { return player.getDuration(); }

    public int getPosn() { return player.getCurrentPosition(); }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent noIntent = new Intent(this, FullScreenActivity.class);
        noIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivities(this, 0, new Intent[]{noIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play_green)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentText("Now Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SERVICE DESTROY");
        super.onDestroy();
        if (player != null){
            player.stop();
            player.reset();
            player.release();
        }
        stopSelf();
        stopForeground(true);
    }
}
