package com.intricatech.autodictator;

import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class StandardInterpreter extends AbstractInterpreter {

    public static final StandardInterpreter instance = new StandardInterpreter();

    public static StandardInterpreter getInstance() {
        return instance;
    }

    private StandardInterpreter() {}

    @Override
    public boolean interpret(
            ResultsUnderEvaluation resultsUnderEvaluation,
            String resultsFromRecognizer,
            MainActivity.MasterState masterState) {

        String cumulativeResults = resultsUnderEvaluation.getCumulativeCurrentResults();

        // If the length of the partial/complete results returned from the recognizer differs from
        // the length of ResultsUnderEvaluation.cumulativeCurrentResults, the recognizer has provided
        // new results, and we must update our cumulative results.
        if ( !resultsFromRecognizer.equals(cumulativeResults)) {
            updateResultsUnderEvaluation(resultsUnderEvaluation, resultsFromRecognizer, masterState);
            return true;
        } else return false;
    }

    private void updateResultsUnderEvaluation(
            ResultsUnderEvaluation resultsUnderEvaluation,
            String resultsFromRecognizer,
            MainActivity.MasterState masterState) {
        List<Word> wordList = resultsUnderEvaluation.getCurrentResultsWordList();
        resultsUnderEvaluation.overwriteCurrentResults(resultsFromRecognizer);
        String[] splitterArray = resultsFromRecognizer.split(" ");
        wordList.clear();
        for (String s : splitterArray) {
            boolean isKeyword = masterState == MainActivity.MasterState.EDITING ? true : false;
            wordList.add(new Word(s, isKeyword));
        }

        // todo - Further split words by punctuation marks returned by the recognizer.
    }

}
