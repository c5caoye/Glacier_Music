package miaoyipu.glaciermusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import miaoyipu.glaciermusic.mservice.MusicService;
import miaoyipu.glaciermusic.songs.Song;
import miaoyipu.glaciermusic.songs.SongAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private static final long MUSIC_DURATION = 30000;
    private static final int READ_STORAGE = 11, WRITE_STORAGE = 12;
    private boolean musicBound = false, isActive = false;
    private static ArrayList<Song> songList;
    private static MusicService musicService;
    private Intent playIntent;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongList(songList);
            musicBound = true;
            setOnCompletion();
            setPlayButton();
            setControlBarTitle();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received UI Sync Broadcast");
            if (isActive && intent.getAction().equalsIgnoreCase("action_uisync")) {
                setControlBarTitle();
                setPlayButton();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.control_bar_play).setOnClickListener(play_button_onClickListener);
        findViewById(R.id.main_fab).setOnClickListener(fab_onClickListener);

        handlePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!musicBound) {
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        } else {
            setOnCompletion();
            setControlBarTitle();
            setPlayButton();
        }

        isActive = true;
        registerReceiver(broadcastReceiver, new IntentFilter("action_uisync"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "DESTORY");
        super.onDestroy();
        unbindService(musicConnection);
        stopService(playIntent);
        musicBound = false;
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
        songList = new ArrayList<Song>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor mCursor = getContentResolver().query(musicUri, null, null, null, null);

        if (mCursor != null && mCursor.moveToFirst()) {
            int idCol = mCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleCol = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumCol = mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int durationCol = mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                if (mCursor.getLong(durationCol) >= MUSIC_DURATION) {
//                    Log.d(TAG, String.valueOf(mCursor.getLong(idCol)));

                    long id = mCursor.getLong(idCol);
                    String title = mCursor.getString(titleCol);
                    String artist = mCursor.getString(artistCol);
                    long album = mCursor.getLong(albumCol);
                    Uri artUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"), album);
                    songList.add(new Song(id, title, artist, artUri));
                }
            } while (mCursor.moveToNext());
        }

        Log.d(TAG, String.valueOf(songList.size()));

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.song_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SongAdapter(this, songList);
        mRecyclerView.setAdapter(mAdapter);
    }

    final View.OnClickListener play_button_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppCompatImageView iv = (AppCompatImageView) v;
            Log.d(TAG, "play/pause");
            if (!musicService.isPlaying()) {
                iv.setImageResource(R.drawable.pause);
                musicService.pausePlay();
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
                Toast.makeText(getApplicationContext(), "shuffle off", Toast.LENGTH_SHORT).show();
                fab.setImageResource(R.drawable.loop);
                musicService.setShuffle();
            } else {
                Toast.makeText(getApplicationContext(), "shuffle on", Toast.LENGTH_SHORT).show();
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

        FloatingActionButton shuffle_btn = (FloatingActionButton) findViewById(R.id.main_fab);
        if (musicService != null && musicService.isShuffle()) {
            shuffle_btn.setImageResource(R.drawable.shuffle);
        } else {
            shuffle_btn.setImageResource(R.drawable.loop);
        }
    }

    public void songPicked(View view) {
        musicService.setSongAndPlay(Integer.parseInt(view.getTag().toString()));
        setControlBarTitle();
        ImageView play_btn = (ImageView)findViewById(R.id.control_bar_play);
        play_btn.setImageResource(R.drawable.pause);
    }

    private void handlePermission() {
        int r_storage_check = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int w_storage_check = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (r_storage_check == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
        }

        /* Why writing permission is needed? */
        if (w_storage_check == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE);
       }
        setSongAdapter();
    }

    private void setOnCompletion() {
        musicService.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                musicService.playNext();
                setControlBarTitle();
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
