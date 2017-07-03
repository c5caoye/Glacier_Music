package miaoyipu.glaciermusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import miaoyipu.glaciermusic.mservice.MusicService;
import miaoyipu.glaciermusic.songs.Songs;

/**
 * Created by cy804 on 2017-05-25.
 */

public class FullScreenActivity extends AppCompatActivity {
    private static final String TAG = "FULL_SCREEN";

    public static MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            Log.d(TAG, "Music Service binded");
            musicBound = true;

            musicService.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    musicService.playNext();
                    setInfo();
                    setPlayButton();
                }
            });

            setInfo();
            setPlayButton();
            initSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!musicBound) {
            Log.d(TAG, "Binding Service");
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(playIntent);
        unbindService(musicConnection);
        musicService = null;
        musicBound = false;
    }

    private void setPlayButton() {
        ImageView play_btn = (ImageView) findViewById(R.id.fullscreen_play);
        if (musicService != null && musicService.isPlaying()) {
            play_btn.setImageResource(R.drawable.pause_green);
        } else {
            play_btn.setImageResource(R.drawable.play_green);
        }
    }

    private void setInfo() {
        if (musicService == null) { Log.e(TAG, "Music Service Null!");}
        Songs song = musicService.getSong();
        ImageView cover = (ImageView) findViewById(R.id.fullscreen_album);
        TextView title = (TextView) findViewById(R.id.fullscreen_tittle);
        TextView artist = (TextView) findViewById(R.id.fullscreen_artist);
        Bitmap album = song.getAlbum_cover();
        if (album != null) {
            cover.setImageBitmap(Bitmap.createScaledBitmap(album, 500, 500, false));
        }
        title.setText(song.getTitle());
        artist.setText(song.getArtist());
    }

    private void initSeekBar() {
        final SeekBar seekBar = (SeekBar)findViewById(R.id.fullscreen_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) musicService.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        }
        );

        final Handler handler = new Handler();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setMax(musicService.getDuration());
                seekBar.setProgress(musicService.getPosn());
                handler.postDelayed(this, 1000);
            }
        });
    }
}
