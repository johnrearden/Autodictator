package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 11/02/2018.
 */

public class ReadInterpreter extends AbstractInterpreter {

    public static String TAG;

    public static final ReadInterpreter instance = new ReadInterpreter();
    public static ReadInterpreter getInstance() {
        return instance;
    }

    private ReadInterpreter() {TAG = getClass().getSimpleName();}

    private InterpreterClient client;

    public void configure(InterpreterClient client) {
        this.client = client;
    }

    @Override
    public void interpret(String resultsFromRecognizer, Results results) {

        String[] splitterArray = resultsFromRecognizer.split(" ");
        String lastWord = splitterArray[splitterArray.length - 1];
        //Log.d(TAG, "last word in resultsFromRecognizer == " + lastWord);

        if (lastWord.toUpperCase().equals("EVERYTHING")) {
            //Log.d(TAG, "'EVERYTHING' heard .... reading");
            //Log.d(TAG, resultsFromRecognizer);
            client.getSpeaker().addSpeechToQueue(client.getDocument().returnEntireDocumentAsString());
            client.onFinishedInterpreting();
        }
    }
}
