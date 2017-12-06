package miaoyipu.glaciermusic.songs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import miaoyipu.glaciermusic.R;

/**
 * Created by cy804 on 2017-05-21.
 */

public class SongAdapter extends BaseAdapter{

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private Context context;

    public SongAdapter(Context c, ArrayList<Song> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(c);
        this.context = c;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.song_adapter_layout, parent, false);

        ImageView album_view = (ImageView) songLay.findViewById(R.id.song_album);
        TextView title_view = (TextView) songLay.findViewById(R.id.song_title);
        TextView artist_view = (TextView) songLay.findViewById(R.id.song_artist);

        Song cur_song = songs.get(position);

        Glide.with(context)
                .load(cur_song.getAlbumUri())
                .asBitmap()
                .placeholder(R.drawable.default_album)
                .into(album_view);

        title_view.setText(cur_song.getTitle());
        artist_view.setText(cur_song.getArtist());

        songLay.setTag(position);

        return songLay;
     }
}
