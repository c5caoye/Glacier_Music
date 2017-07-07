package miaoyipu.glaciermusic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import miaoyipu.glaciermusic.mservice.MusicService;


/**
 * Created by cy804 on 2017-05-25.
 */

public class FullScreenActivity extends AppCompatActivity {
    private static final String TAG = "FULL_SCREEN";
    private static MusicService musicService;
    private boolean musicBound = false;
    private boolean active = false;
    private Intent playIntent;
    private ImageView prevBtn, playBtn, nextBtn, shuffleBtn;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicBound = true;
            setOnCompletion();
            setButton();
            setInfo();
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

        prevBtn = (ImageView)findViewById(R.id.fullscreen_prev);
        prevBtn.setOnClickListener(playPrev);
        playBtn = (ImageView)findViewById(R.id.fullscreen_play);
        playBtn.setOnClickListener(playPause);
        nextBtn = (ImageView) findViewById(R.id.fullscreen_next);
        nextBtn.setOnClickListener(playNext);
        shuffleBtn = (ImageView) findViewById(R.id.fullscreen_shuffle);
        shuffleBtn.setOnClickListener(playShuffle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        if (!musicBound) {
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }else {
            setOnCompletion();
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
        stopService(playIntent);
        musicBound = false;
    }

    private void setButton() {
        if (musicService != null && musicService.isPlaying()) {
            playBtn.setImageResource(R.drawable.pause_green);
        } else {
            playBtn.setImageResource(R.drawable.play_green);
        }
        if (musicService != null && musicService.isShuffle()) {
            shuffleBtn.setImageResource(R.drawable.shuffle_green_dark);
        } else { shuffleBtn.setImageResource(R.drawable.shuffle_green); }
    }

    private void setInfo() {
        ImageView cover = (ImageView) findViewById(R.id.fullscreen_album);
        TextView title = (TextView) findViewById(R.id.fullscreen_tittle);
        TextView artist = (TextView) findViewById(R.id.fullscreen_artist);

        Glide.with(this)
                .load(musicService.getAlbumUri())
                .asBitmap()
                .placeholder(R.drawable.default_album)
                .error(R.drawable.default_album)
                .into(cover);

        title.setText(musicService.getTitle());
        artist.setText(musicService.getArtist());
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
            public void run(){
                if (active) {
                    if (musicService.isPlaying()) {
                        seekBar.setMax(musicService.getDuration());
                        seekBar.setProgress(musicService.getPosn());
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void setOnCompletion() {
        musicService.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                musicService.playNext();
                setInfo();
                setButton();
            }
        });
    }

    final View.OnClickListener playPrev = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            musicService.playPrev();
            setInfo();
            playBtn.setImageResource(R.drawable.pause_green);
        }
    };

    final View.OnClickListener playPause = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (musicService.isPlaying()) {
                musicService.pause();
                playBtn.setImageResource(R.drawable.play_green);
            } else {
                musicService.pausePlay();
                playBtn.setImageResource(R.drawable.pause_green);
            }
            setInfo();
        }
    };

    final View.OnClickListener playNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            musicService.playNext();
            setInfo();
            playBtn.setImageResource(R.drawable.pause_green);
        }
    };

    final View.OnClickListener playShuffle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            musicService.setShuffle();
            setButton();
        }
    };

}
