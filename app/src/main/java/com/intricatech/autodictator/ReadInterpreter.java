package com.intricatech.autodictator;

import android.util.Log;

/**
 * Created by Bolgbolg on 11/02/2018.
 */

public class ReadInterpreter extends AbstractInterpreter {

    public static final String TAG = "ReadInterpreter";

    public static final ReadInterpreter instance = new ReadInterpreter();

    public static ReadInterpreter getInstance() {
        return instance;
    }

    private static SpeakerFacade textSpeaker;

    public void configure(SpeakerFacade speaker) {
        textSpeaker = speaker;
    }

    private ReadInterpreter() {}

    @Override
    public boolean interpret(ResultsUnderEvaluation resultsUnderEvaluation,
                             String resultsFromRecognizer,
                             MainActivity.MasterState masterState) {

        String[] splitterArray = resultsFromRecognizer.split(" ");
        String lastWord = splitterArray[splitterArray.length - 1];
        Log.d(TAG, "last word in resultsFromRecognizer == " + lastWord);

        if (lastWord.toUpperCase().equals("EVERYTHING")) {
            Log.d(TAG, "'EVERYTHING' heard .... reading");
            Log.d(TAG, resultsFromRecognizer);
            textSpeaker.addSpeechToQueue("This is just a test utterance, bay");
        }

        return false;
    }
}
