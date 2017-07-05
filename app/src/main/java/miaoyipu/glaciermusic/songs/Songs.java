package miaoyipu.glaciermusic.songs;

    import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

    import com.bumptech.glide.load.DecodeFormat;
    import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
    import com.bumptech.glide.load.resource.bitmap.BitmapDecoder;

    import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cy804 on 2017-05-21.
 */

public class Songs {
    private long id;
    private String title, artist;
    private Bitmap album_cover;

    public Songs(long id, String title, String artist, Bitmap album_cover) {
        this.id = id; this.title = title; this.artist = artist; this.album_cover = album_cover;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Bitmap getAlbum_cover() {
        return album_cover;
    }

    public static ArrayList<Songs> getSongList(ContentResolver musicResolver, Resources res) {
        ArrayList<Songs> song_list = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int id_col = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int title_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int album_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do {
                long id = musicCursor.getLong(id_col);
                String title = musicCursor.getString(title_col);
                String artist = musicCursor.getString(artist_col);
                long album = musicCursor.getLong(album_col);

                Uri album_Uri = Uri.parse("content://media/external/audio/albumart");
                Uri art_Uri = ContentUris.withAppendedId(album_Uri, album);

                Bitmap bm = null;

                try {

                    bm = MediaStore.Images.Media.getBitmap(musicResolver, art_Uri);
                }  catch (IOException e) {
                    e.printStackTrace();
                }

                song_list.add(new Songs(id, title, artist, bm));
            } while (musicCursor.moveToNext());
        }

        return song_list;
    }

}
