package com.intricatech.autodictator;

import android.util.Log;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class StandardInterpreter extends AbstractInterpreter {

    public static String TAG;
    public static final StandardInterpreter instance = new StandardInterpreter();

    public static StandardInterpreter getInstance() {
        return instance;
    }

    private InterpreterClient client;

    private StandardInterpreter() {
        TAG = getClass().getSimpleName();
        Log.d(TAG, "private constructor invoked");
    }

    public void configure(InterpreterClient client) {
        this.client = client;
    }

    @Override
    public void interpret(String resultsFromRecognizer, Results results) {

        // If the length of the partial/complete results returned from the recognizer differs from
        // the length of Results.cumulativeCurrentResults, the recognizer has provided
        // new results, and we must update our cumulative results.
        if (results.isNewResult(resultsFromRecognizer)) {
            results.updateCurrentResultsWordList(
                    resultsFromRecognizer,
                    client.getMasterState());
            client.onNewWordFound();
        }
    }
}
