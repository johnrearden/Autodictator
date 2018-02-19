package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class StandardInterpreter extends AbstractInterpreter {

    public static String TAG;
    public static final StandardInterpreter instance = new StandardInterpreter();

    public static StandardInterpreter getInstance() {
        return instance;
    }

    private StandardInterpreter() {
        TAG = getClass().getSimpleName();
    }

    @Override
    public InterpreterReturnPacket interpret(
            Document document,
            ResultsUnderEvaluation resultsUnderEvaluation,
            String resultsFromRecognizer,
            MainActivity.MasterState masterState,
            boolean isSpeaking) {

        String cumulativeResults = resultsUnderEvaluation.getCumulativeCurrentResults();

        // If the length of the partial/complete results returned from the recognizer differs from
        // the length of ResultsUnderEvaluation.cumulativeCurrentResults, the recognizer has provided
        // new results, and we must update our cumulative results.
        if ( !resultsFromRecognizer.equals(cumulativeResults)) {
            resultsUnderEvaluation.updateCurrentResultsWordList(
                    resultsFromRecognizer,
                    masterState,
                    isSpeaking);
            return new InterpreterReturnPacket(true, false);
        } else return new InterpreterReturnPacket(false, false);
    }
}
