package miaoyipu.glaciermusic.mservice;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

import miaoyipu.glaciermusic.songs.Song;

/**
 * Created by cy804 on 2017-07-15.
 */

class Utility {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_UISYNC = "action_uisync";

}
