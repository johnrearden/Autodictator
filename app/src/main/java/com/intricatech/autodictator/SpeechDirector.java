package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 21/02/2018.
 */

public interface SpeechDirector {

    public void register(SpeechObserver observer);

    public void unregister(SpeechObserver observer);

    public void onSpeechStarted();

    public void onSpeechEnded();

}
