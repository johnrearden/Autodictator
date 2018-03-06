package com.intricatech.autodictator;

import android.util.Log;

/**
 * Created by Bolgbolg on 06/03/2018.
 */

public class KeywordResults extends Results {

    public KeywordResults() {
        super();
    }

    /**
     * Fulfills the same function as the eponymous superclass method (which has a different signature)
     * but ignores all words occurring before the primary keyword (used to choose the appropriate
     * Interpreter - e.g. READ, SELECT, DELETE etc.
     * @param resultsFromRecognizer A partial/full results String from the recognizer.
     * @param selector Identifies the selector that is calling the method, to facilitate splitting
     *                  of the resultsFromRecognizer String.
     * @return TRUE if a new keyword has been detected, FALSE otherwise.
     */
    public boolean isNewResult(String resultsFromRecognizer, InterpreterSelector selector) {
        String cumulativeResults = getPreviousRecognizerOutput();

        // If the length of the partial/complete results returned from the recognizer differs from
        // the length of Results.cumulativeCurrentResults, the recognizer has provided
        // new results, and we must update our cumulative results.
        if ( !resultsFromRecognizer.equals(cumulativeResults)) {
            return true;
        } else return false;
    }

    public void updateCurrentResultsWordList(
            String recognizerOutput,
            InterpreterSelector selector) {
        overwriteCurrentResults(recognizerOutput, selector);
        String[] splitterArray = recognizerOutput.split(" ");
        wordList.clear();
        WordType wordType;
        for (String s : splitterArray) {
            if (s.length() > 0) {
                wordType = WordType.KEYWORD;
                wordList.add(new Word(s, wordType));
            }
        }
        Log.d(TAG, this.toString());

        // todo - Further split words by punctuation marks returned by the recognizer.
    }

    private void overwriteCurrentResults(
            String cumulativeCurrentResults,
            InterpreterSelector selector) {
        String splitOnKeyword = splitStringOnKeyword(
                cumulativeCurrentResults,
                InterpreterSelector.READ_INTERPRETER.toString());
        String[] splitBySpaces = splitOnKeyword.split(" "); // todo - unfragilate this.

        wordList.clear();
        for (String s : splitBySpaces) {
            wordList.add(new Word(s, WordType.KEYWORD));
        }
        Log.d(TAG, selector.toString() + " keyword string : " + splitBySpaces);
    }


}
