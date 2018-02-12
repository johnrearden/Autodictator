package com.intricatech.autodictator;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class TextSpeaker implements SpeakerFacade {

    private TextToSpeech ttsEngine;
    private boolean revertToNormalPitchAfterNextUtterance;

    public TextSpeaker(Context context) {
        ttsEngine = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttsEngine.setLanguage(Locale.UK);
                ttsEngine.setSpeechRate(1.5f);
            }
        });
        revertToNormalPitchAfterNextUtterance = false;
    }

    @Override
    public void speakImmediately(String utterance) {
        ttsEngine.speak(utterance, TextToSpeech.QUEUE_FLUSH, null, null);
        if (revertToNormalPitchAfterNextUtterance) {
            ttsEngine.setPitch(1.0f);
        }
    }

    @Override
    public void addSpeechToQueue(String utterance) {
        ttsEngine.speak(utterance, TextToSpeech.QUEUE_ADD, null, null);
    }

    public void setSpeechRate(float newRate) {
        ttsEngine.setSpeechRate(newRate);
    }
}
