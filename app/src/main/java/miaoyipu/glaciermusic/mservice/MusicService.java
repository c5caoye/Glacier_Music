package miaoyipu.glaciermusic.mservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import miaoyipu.glaciermusic.FullScreenActivity;
import miaoyipu.glaciermusic.R;
import miaoyipu.glaciermusic.songs.Song;

/**
 * Created by cy804 on 2017-06-05.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    private final IBinder mBind = new MusicBinder();
    private static final String TAG = "MService";
    private static final int NOTIFY_ID = 13;
    private static MediaPlayer player;

    private Song[] songList, shuffleList;
    private int songListSize, currentVolume, curPosn;
    private String songTitle = "Glacier Music", songArtist = "Unknown";
    private AudioManager audioManager;
    private boolean shuffle = false;
    private Uri songUri;
    private boolean isRunning = false;

    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver noisyReceiver = new BecomingNoisyReceiver();

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

    public void setSongList(Song[] theSongs) {
        Log.d(TAG, "Setting song list");
        songList = theSongs;
        shuffleList = (Song[]) songList.clone();
        Collections.shuffle(Arrays.asList(shuffleList));
        songListSize = theSongs.length;
        curPosn = 0;
    }

    public void setShuffle() {
        Log.d(TAG, "Toggle Shuffle");
        shuffle = !shuffle;
    }

    public void pause() {
        Log.d(TAG, "PAUSE");
        player.pause();
        buildNotification(generateAction(R.drawable.ic_play, "Play", Utility.ACTION_PLAY));
    }

    public void pausePlay() {
        buildNotification(generateAction(R.drawable.ic_pause, "Pause", Utility.ACTION_PAUSE));
        if (isRunning) {
            player.start();
        } else {
            play();
        }
    }

    public void play() {
        player.reset();
        Song song;

        if (shuffle) {
            song = shuffleList[curPosn];
        } else {
            song = songList[curPosn];
        }

        startPlay(song);
    }

    public void play(int idx) {
        player.reset();
        Song song = songList[idx];
        startPlay(song);
    }

    public void startPlay(Song song) {
        registerReceiver(noisyReceiver, intentFilter);

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
        buildNotification(generateAction(R.drawable.ic_pause, "Pause", Utility.ACTION_PAUSE));
        sendUIBroadcast();
    }

    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }


    private void buildNotification(Notification.Action action) {
        Notification.MediaStyle style = new Notification.MediaStyle();
        Intent intent = new Intent(getApplicationContext(), FullScreenActivity.class);
        intent.setAction(Utility.ACTION_STOP);
        PendingIntent deleteIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.play)
                .setContentTitle("Glacier Music")
                .setContentTitle(this.songTitle)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deleteIntent)
                .setStyle(style);

        builder.addAction(generateAction(R.drawable.ic_prev, "Previous", Utility.ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.ic_next, "Next", Utility.ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SERVICE DESTROY");
        super.onDestroy();

        unregisterReceiver(noisyReceiver);

        if (player != null){
            player.stop();
            player.reset();
            player.release();
        }
        stopSelf();
        stopForeground(true);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) { return; }

        String action = intent.getAction();
        if (action.equalsIgnoreCase(Utility.ACTION_PLAY)) {
            this.pausePlay();
        } else if (action.equalsIgnoreCase(Utility.ACTION_PAUSE)) {
            this.pause();
        } else if (action.equalsIgnoreCase(Utility.ACTION_NEXT)) {
            this.playNext();
        } else if (action.equalsIgnoreCase(Utility.ACTION_PREVIOUS)) {
            this.playPrev();
        } else if (action.equalsIgnoreCase(Utility.ACTION_STOP)) {
            Log.d(TAG, "ACTION_STOP");
            this.pause();
        }

        sendUIBroadcast();
    }

    private void sendUIBroadcast() {
        Log.d(TAG, "Send UI Sync Broadcast");
        Intent broadcastIntent = new Intent(Utility.ACTION_UISYNC);
        sendBroadcast(broadcastIntent);
    }
}




