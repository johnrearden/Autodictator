package com.intricatech.autodictator;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 08/02/2018.
 */

public class Results {

    protected static String TAG;

    protected  String previousRecognizerOutput;
    protected  List<Word> wordList;

    public Results() {
        TAG = getClass().getSimpleName();
        wordList = new LinkedList<>();
    }

    public void updateCurrentResultsWordList(String recognizerOutput, MasterState masterState) {
        overwriteCurrentResults(recognizerOutput);
        String[] splitterArray = recognizerOutput.split(" ");
        wordList.clear();
        WordType wordType;
        for (String s : splitterArray) {
            if (s.length() > 0) {
                wordType = masterState == MasterState.EDITING ?
                            WordType.KEYWORD : WordType.NORMAL;
                wordList.add(new Word(s, wordType));
            }
        }
        Log.d(TAG, this.toString());

        // todo - Further split words by punctuation marks returned by the recognizer.
    }

    public String getPreviousRecognizerOutput() {
        return previousRecognizerOutput;
    }

    protected void overwriteCurrentResults(String cumulativeCurrentResults) {
        this.previousRecognizerOutput = cumulativeCurrentResults;
    }

    public List<Word> getWordList() {
        return wordList;
    }

    public void clearWordList() {
        wordList.clear();
    }

    public Word getLastWord() {
        int size = wordList.size();
        if (size == 0) {
            return new Word("", WordType.NORMAL); // return a null object.
        } else {
            return wordList.get(wordList.size() - 1);
        }

    }

    /*public String getLastTwoWordsAsString() {
        int size = wordList.size();
        if (size < 2) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(wordList.get(size - 2).getWordString());
            sb.append(" ");
            sb.append(wordList.get(size - 1).getWordString());
            return sb.toString();
        }
    }*/

    public String getLastNWordsAsString(int n) {
        int size = wordList.size();
        if (size < n) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = size - n; i < size - 1; i++) {
                sb.append(wordList.get(i).getWordString());
                sb.append(" ");
            }
            sb.setLength(sb.length() - 1); // Remove final trailing space.
            return sb.toString();

        }
    }

    public void ignoreLastNumberOfWords(int numberOfWordsToIgnore) {
        int size = wordList.size();
        for (int i = size - numberOfWordsToIgnore; i < size; i++) {
            wordList.get(i).declareKeyword(true);
            Log.d(TAG, toString());
        }
    }

    /**
     * Method performs a simple length comparison of the latest string from the recognizer
     * and the accumulated results (in String form) received so far, and informs the caller if the
     * current recognizer String contains any new words.
     * @param resultsFromRecognizer A partial/full results String from the recognizer.
     * @return TRUE if the String contains a new word, FALSE otherwise.
     */
    public boolean isNewResult(String resultsFromRecognizer) {
        String cumulativeResults = getPreviousRecognizerOutput();

        // If the length of the partial/complete results returned from the recognizer differs from
        // the length of Results.cumulativeCurrentResults, the recognizer has provided
        // new results, and we must update our cumulative results.
        if ( !resultsFromRecognizer.equals(cumulativeResults)) {
            return true;
        } else return false;    }

    /**
     * Utility method that discards everything up to and including the keyword that switches
     * Interpreter from a recognizerResults String.
     * @param source The String to be pruned.
     * @param keyword The selector keyword that preceeds the required String.
     * @return The pruned String.
     */
    public static String splitStringOnKeyword(String source, String keyword) {
        String upperCaseVersion = source.toUpperCase();
        String[] splitBySelector = upperCaseVersion.split(keyword);
        int index = splitBySelector.length - 1; // We need the last String in the array.
        return splitBySelector[index];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Results.wordList\n");
        for (Word w : wordList) {
            sb.append(w.getWordString() + "(" + w.getType().toString() + ") ");
        }
        return sb.toString();
    }

    public static String getWordListAsString(List<Word> list) {
        StringBuilder sb = new StringBuilder("Results.wordList\n");
        for (Word w : list) {
            sb.append(w.getWordString() + "(" + w.getType().toString() + ") ");
        }
        return sb.toString();
    }
}
