package com.intricatech.autodictator;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Bolgbolg on 02/03/2018.
 */

public class SoundMeter {

    private static String TAG;
    private static double THRESHOLD = 2000;

    private MediaRecorder recorder;

    public SoundMeter() {
        TAG = getClass().getSimpleName();
    }
    public void start() {
        if (recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
        }
    }

    public void stop() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                Log.d(TAG, "Pointless exception thrown, see MediaRecorder.pause()");
            } finally {
                recorder.release();
                recorder = null;
            }
        }
    }

    public boolean isOverThreshold() {
        double d = recorder.getMaxAmplitude();
        //Log.d(TAG, "maxAmplititude since last sample : " + d);
        if (d > THRESHOLD) {
            return true;
        } else return false;
    }

}
