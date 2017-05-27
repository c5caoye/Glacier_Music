package miaoyipu.glaciermusic;

import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by cy804 on 2017-05-25.
 */

public class FullScreenActivity extends AppCompatActivity {
    private static final String TAG = "Full Screen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen);
    }
}
