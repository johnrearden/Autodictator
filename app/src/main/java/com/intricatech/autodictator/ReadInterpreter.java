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

    public static void configure(SpeakerFacade speaker) {
        textSpeaker = speaker;
    }

    private ReadInterpreter() {}

    @Override
    public InterpreterReturnPacket interpret(Document document,
                             ResultsUnderEvaluation resultsUnderEvaluation,
                             String resultsFromRecognizer,
                             MainActivity.MasterState masterState,
                             boolean isSpeaking) {

        String[] splitterArray = resultsFromRecognizer.split(" ");
        String lastWord = splitterArray[splitterArray.length - 1];
        Log.d(TAG, "last word in resultsFromRecognizer == " + lastWord);

        if (lastWord.toUpperCase().equals("EVERYTHING")) {
            Log.d(TAG, "'EVERYTHING' heard .... reading");
            Log.d(TAG, resultsFromRecognizer);
            textSpeaker.addSpeechToQueue(document.returnEntireDocumentAsString());
            return new InterpreterReturnPacket(false, true);
        }

        return new InterpreterReturnPacket(false, false);
    }
}
