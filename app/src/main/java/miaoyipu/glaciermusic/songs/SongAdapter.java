package miaoyipu.glaciermusic.songs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import miaoyipu.glaciermusic.R;

/**
 * Created by cy804 on 2017-05-21.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private Song[] songs;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView albumView;
        public TextView titleView, artistView;
        public View mainView; // The parent view. This is added for onClick to work.

        public ViewHolder(View v) {
            super(v);
            albumView = (ImageView) v.findViewById(R.id.song_album);
            titleView = (TextView) v.findViewById(R.id.song_title);
            artistView = (TextView) v.findViewById(R.id.song_artist);
            mainView = v;
        }
    }

    public SongAdapter(Context c, Song[] data) {
        songs = data;
        context = c;
    }

    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_adapter_layout, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song curSong = songs[position];

        holder.mainView.setTag(position); // For onClick to work.
        holder.titleView.setText(curSong.getTitle());
        holder.artistView.setText(curSong.getArtist());
        Glide.with(context)
                .load(curSong.getAlbumUri())
                .asBitmap()
                .placeholder(R.drawable.default_album)
                .into(holder.albumView);
    }

    @Override
    public int getItemCount() {
        return songs.length;
    }
}