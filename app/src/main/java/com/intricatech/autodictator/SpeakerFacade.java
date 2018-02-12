package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public interface SpeakerFacade {

    public void speakImmediately(String utterance);

    public void addSpeechToQueue(String utterance);
}
