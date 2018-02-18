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
            updateResultsUnderEvaluation(
                    resultsUnderEvaluation,
                    resultsFromRecognizer,
                    masterState,
                    isSpeaking);
            return true;
        } else return false;
    }

    private void updateResultsUnderEvaluation(
            ResultsUnderEvaluation resultsUnderEvaluation,
            String resultsFromRecognizer,
            MainActivity.MasterState masterState,
            boolean isSpeaking) {
        List<Word> wordList = resultsUnderEvaluation.getCurrentResultsWordList();
        resultsUnderEvaluation.overwriteCurrentResults(resultsFromRecognizer);
        String[] splitterArray = resultsFromRecognizer.split(" ");
        wordList.clear();
        WordType wordType;
        for (String s : splitterArray) {
            if (isSpeaking) {
                wordType = WordType.IGNORED;
            } else {
                wordType = masterState == MainActivity.MasterState.EDITING ?
                        WordType.KEYWORD : WordType.NORMAL;
            }
            wordList.add(new Word(s, wordType));

        }

        // todo - Further split words by punctuation marks returned by the recognizer.
    }

}
