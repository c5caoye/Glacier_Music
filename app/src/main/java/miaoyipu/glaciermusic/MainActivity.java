package miaoyipu.glaciermusic;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private static final int READ_STORAGE = 11;
    private boolean is_pln = false; // Delete this later.
    private boolean is_shuffle = false;
    private ArrayList<Songs> song_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.control_bar_play).setOnClickListener(play_button_onClickListener);
        findViewById(R.id.fab).setOnClickListener(fab_onClickListener);

        int storage_check = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (storage_check == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
        } else {setSongAdapter();}
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
            if (is_pln) {
                iv.setImageResource(R.drawable.pause);
                is_pln = false;
            } else {
                iv.setImageResource(R.drawable.play);
                is_pln = true;
            }
        }
    };

    final View.OnClickListener fab_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FloatingActionButton fab = (FloatingActionButton)v;
            if (is_shuffle) {
                is_shuffle = false;
                fab.setImageResource(R.drawable.loop);
            } else {
                is_shuffle = true;
                fab.setImageResource(R.drawable.shuffle);
            }
        }
    };
}
