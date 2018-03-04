package com.intricatech.autodictator;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class TextSpeaker implements SpeakerFacade, SpeechDirector{

    private static String TAG;
    private TextToSpeech ttsEngine;
    private int utteranceID;
    private Handler handler;

    private List<SpeechObserver> observers;

    public TextSpeaker(Context context, Handler handler) {
        TAG = getClass().getSimpleName();
        this.handler = handler;
        utteranceID = 0;
        ttsEngine = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(TAG, "onInit() callback invoked");
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "Success!");
                }
                ttsEngine.setLanguage(Locale.UK);
                ttsEngine.setSpeechRate(1.5f);
                ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        onSpeechStarted();
                        Log.d(TAG, "onStart() invoked ... " + utteranceId);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        onSpeechEnded();
                        Log.d(TAG, "onDone() invoked... " + utteranceId);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        onSpeechEnded();
                        Log.d(TAG, "onError() invoked... " + utteranceId);
                    }
                });
            }
        });
        observers = new ArrayList<>();
    }

    @Override
    public void speakImmediately(String utterance) {
        String idString = "utterance_" + utteranceID++;
        ttsEngine.speak(utterance, TextToSpeech.QUEUE_FLUSH, null, idString);
    }

    @Override
    public void addSpeechToQueue(String utterance) {
        String idString = "utterance_" + utteranceID++;
        ttsEngine.speak(utterance, TextToSpeech.QUEUE_ADD, null, idString);
    }

    public void setSpeechRate(float newRate) {
        ttsEngine.setSpeechRate(newRate);
    }

    @Override
    public void register(SpeechObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(SpeechObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void onSpeechStarted() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (SpeechObserver ob : observers) {
                    ob.onSpeechStarted();
                }
            }
        });
    }

    @Override
    public void onSpeechEnded() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (SpeechObserver ob : observers) {
                    ob.onSpeechEnded();
                }
            }
        });
    }
}
