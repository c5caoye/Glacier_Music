package miaoyipu.glaciermusic;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import miaoyipu.glaciermusic.mservice.MusicService;
import miaoyipu.glaciermusic.songs.Songs;
import miaoyipu.glaciermusic.songs.SongsAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private static final int READ_STORAGE = 11;
    private static final int WRITE_STORAGE = 12;
    private ArrayList<Songs> song_list;

    public static MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;

    private PowerManager.WakeLock wakeLock;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongList(song_list);
            musicBound = true;

            musicService.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    musicService.playNext();
                    setControlBarTitle();
                }
            });

            setPlayButton();
            setControlBarTitle();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.control_bar_play).setOnClickListener(play_button_onClickListener);
        findViewById(R.id.fab).setOnClickListener(fab_onClickListener);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MAIN LOCK");
        wakeLock.acquire();

        handlePermission();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!musicBound) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(playIntent);
        unbindService(musicConnection);
        musicService = null;
        musicBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
//        stopService(playIntent);
//        unbindService(musicConnection);
//        musicService = null;
//        musicBound = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.d(TAG, "Settings been clicked.");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_UP) :
                Log.d(TAG, "Slide up, to full screen");
                Intent intent = new Intent(this, FullScreenActivity.class);
                startActivity(intent);
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }

    private void setSongAdapter() {
        song_list = Songs.getSongList(getContentResolver(), getResources());

        Collections.sort(song_list, new Comparator<Songs>() {
            @Override
            public int compare(Songs o1, Songs o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        SongsAdapter songAdpter = new SongsAdapter(this, song_list);
        ListView songs_view = (ListView) findViewById(R.id.song_list);
        songs_view.setAdapter(songAdpter);
    }

    final View.OnClickListener play_button_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppCompatImageView iv = (AppCompatImageView) v;
            Log.d(TAG, "play/pause");
            if (!musicService.isPlaying()) {
                iv.setImageResource(R.drawable.pause);
                musicService.play();
                setControlBarTitle();
            } else {
                iv.setImageResource(R.drawable.play);
                musicService.pause();
            }
        }
    };

    final View.OnClickListener fab_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FloatingActionButton fab = (FloatingActionButton)v;
            if (musicService.isShuffle()) {
                fab.setImageResource(R.drawable.loop);
                musicService.setShuffle();
            } else {
                fab.setImageResource(R.drawable.shuffle);
                musicService.setShuffle();
            }
        }
    };

    public void setControlBarTitle() {
        TextView view = (TextView) findViewById(R.id.control_bar_title);
        if (musicService != null) {
            view.setText(musicService.getTitle());
        }
    }

    private void setPlayButton() {
        ImageView play_btn = (ImageView) findViewById(R.id.control_bar_play);
        if (musicService != null && musicService.isPlaying()) {
            play_btn.setImageResource(R.drawable.pause);
        } else {
            play_btn.setImageResource(R.drawable.play);
        }
    }

    public void songPicked(View view) {
        musicService.setSongAndPlay(Integer.parseInt(view.getTag().toString()));
        setControlBarTitle();
        ImageView play_btn = (ImageView)findViewById(R.id.control_bar_play);
        play_btn.setImageResource(R.drawable.pause);
    }

    private void handlePermission() {
        int storage_check = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int w_storage_check = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storage_check == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
        }
        if (w_storage_check == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
        }
        setSongAdapter();
    }
}
