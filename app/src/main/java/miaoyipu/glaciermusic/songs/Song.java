package miaoyipu.glaciermusic.songs;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.ArrayList;

/**
 * Class to store info for each single song.
 */

public class Song {
    private long id;
    private String title, artist;
    private Uri albumUri; // Album cover

    public Song(long id, String title, String artist, Uri albumUri) {
        this.id = id; this.title = title; this.artist = artist; this.albumUri = albumUri;
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

    public Uri getAlbumUri() { return albumUri; }

    /* Moved to Main Activity */
//    public static ArrayList<Song> getSongList(ContentResolver musicResolver, Resources res) {
//        ArrayList<Song> song_list = new ArrayList<>();
//        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
//
//        if (musicCursor != null && musicCursor.moveToFirst()) {
//            int id_col = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
//            int title_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
//            int artist_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
//            int album_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
//            int duration_col = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
//
//            do {
//                if (musicCursor.getLong(duration_col) >= 30000) {
//                    long id = musicCursor.getLong(id_col);
//                    String title = musicCursor.getString(title_col);
//                    String artist = musicCursor.getString(artist_col);
//                    long album = musicCursor.getLong(album_col);
//
//                    Uri albumUri = Uri.parse("content://media/external/audio/albumart");
//                    Uri artUri = ContentUris.withAppendedId(albumUri, album);
//
//                    song_list.add(new Song(id, title, artist, artUri));
//                }
//            } while (musicCursor.moveToNext());
//        }
//
//        return song_list;
//    }

}
