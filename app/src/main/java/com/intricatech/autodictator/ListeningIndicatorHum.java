package com.intricatech.autodictator;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by Bolgbolg on 02/03/2018.
 */

public class ListeningIndicatorHum implements MediaPlayer.OnPreparedListener{

    private MediaPlayer player;
    private boolean ready;
    private boolean isPlaying;

    private static String TAG;
    private static final float DEFAULT_VOLUME = 0.2f;

    public ListeningIndicatorHum(Context context) {
        ready = false;
        isPlaying = false;
        player = MediaPlayer.create(context, R.raw.autodictator_hum);
        player.setOnPreparedListener(this);
        player.setLooping(true);
        player.setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME);

        TAG = getClass().getSimpleName();
    }

    public void play() {
        if (isPlaying == false) {
            player.start();
            isPlaying = true;
        } else {
            Log.d(TAG, "player is already playing");
        }
    }

    public void pause() {
        if (isPlaying == true) {
            player.pause();
            isPlaying = false;
        } else {
            Log.d(TAG, "player is already paused");
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        ready = true;
    }

    public void onPause() {
        player.stop();
        player.release();
    }
}
