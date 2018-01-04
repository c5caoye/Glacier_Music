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
        public View mainView;

        public ViewHolder(View v) {
            super(v);
            albumView = (ImageView) v.findViewById(R.id.song_album);
            titleView = (TextView) v.findViewById(R.id.song_title);
            artistView = (TextView) v.findViewById(R.id.song_artist);
            mainView = v;
        }
    }

    public SongAdapter(Context c, ArrayList<Song> data) {
        songs = data.toArray(new Song[0]);
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

        holder.mainView.setTag(position);
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



//public class SongAdapter extends RecyclerView.Adapter{
//
//    private ArrayList<Song> songs;
//    private LayoutInflater songInf;
//
//    public static class MyViewHolder extends RecyclerView.ViewHolder {
//        public ImageView albumView;
//        public TextView titleView, artistView;
//
//        public MyViewHolder(View v) {
//            super(v);
//            albumView = (ImageView) v.findViewById(R.id.song_album);
//            titleView = (TextView) v.findViewById(R.id.song_title);
//            artistView = (TextView) v.findViewById(R.id.song_artist);
//        }
//    }
//
//    /** Constructor **/
//    public SongAdapter(ArrayList<Song> songs) {
//        this.songs = songs;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        TextView tView = (TextView) LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.)
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//////    @Override
////    public View getView(int position, View convertView, ViewGroup parent) {
////        SongViewHolder holder;
////
////        if (convertView == null) {
////            convertView = songInf.inflate(R.layout.song_adapter_layout, parent, false);
////            holder = new SongViewHolder();
////            holder.albumView = (ImageView) convertView.findViewById(R.id.song_album);
////            holder.titleView = (TextView) convertView.findViewById(R.id.song_title);
////            holder.artistView = (TextView) convertView.findViewById(R.id.song_artist);
////            convertView.setTag(holder);
////        } else {
////            holder = (SongViewHolder) convertView.getTag();
////        }
////
////        Song song = songs.get(position);
////        Glide.with(context)
////                .load(song.getAlbumUri())
////                .asBitmap()
////                .placeholder(R.drawable.default_album)
////                .into(holder.albumView);
////        holder.titleView.setText(song.getTitle());
////        holder.artistView.setText(song.getArtist());
////
////        return convertView;
////     }
//}
