package miaoyipu.glaciermusic.songs;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by cy804 on 2018-01-04.
 */

public class MusicLibrary {
    public static Song[] songList;
    private static final int MUSIC_DURATION = 30000;

    public MusicLibrary() {}

    public Song[] getSongs(Cursor cursor) {
        if (songList == null) {
            ArrayList<Song> sl = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                do {
                    if (cursor.getLong(durationCol) >= MUSIC_DURATION) {

                        long id = cursor.getLong(idCol);
                        String title = cursor.getString(titleCol);
                        String artist = cursor.getString(artistCol);
                        long album = cursor.getLong(albumCol);
                        Uri artUri = ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"), album);
                        sl.add(new Song(id, title, artist, artUri));
                    }
                } while (cursor.moveToNext());
            }

            Collections.sort(sl, new Comparator<Song>() {
                @Override
                public int compare(Song o1, Song o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });

            songList = sl.toArray(new Song[0]);
        }

        return songList;
    }
}
