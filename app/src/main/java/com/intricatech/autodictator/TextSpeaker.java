package com.intricatech.autodictator;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class TextSpeaker implements SpeakerFacade{

    private static String TAG;
    private TextToSpeech ttsEngine;
    private boolean isSpeaking;
    private int utteranceID;

    public TextSpeaker(Context context) {
        TAG = getClass().getSimpleName();
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
                        isSpeaking = true;
                        Log.d(TAG, "onStart() invoked ... " + utteranceId + ", isSpeaking == " + isSpeaking);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        isSpeaking = false;
                        Log.d(TAG, "onDone() invoked... " + utteranceId + ", isSpeaking == " + isSpeaking);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        isSpeaking = false;
                        Log.d(TAG, "onError() invoked... " + utteranceId + ", isSpeaking == " + isSpeaking);
                    }
                });
            }
        });

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

    @Override
    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void setSpeechRate(float newRate) {
        ttsEngine.setSpeechRate(newRate);
    }
}
