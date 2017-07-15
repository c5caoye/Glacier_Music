package miaoyipu.glaciermusic.mservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by cy804 on 2017-07-15.
 */

class BecomingNoisyReceiver extends BroadcastReceiver {
    private static final String TAG = "NoisyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equalsIgnoreCase(intent.getAction())) {
            Log.d(TAG, "Headphone unplugged.");
            Intent pintent = new Intent(context, MusicService.class);
            pintent.setAction(Utli.ACTION_PAUSE);
            context.startService(pintent);
        }
    }
}
