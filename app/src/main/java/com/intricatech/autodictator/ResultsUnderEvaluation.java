package com.intricatech.autodictator;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 08/02/2018.
 */

public class ResultsUnderEvaluation {

    private static String TAG;

    private String cumulativeCurrentResults;
    private List<Word> currentResultsWordList;

    public ResultsUnderEvaluation() {
        TAG = getClass().getSimpleName();
        currentResultsWordList = new LinkedList<>();
    }

    public void updateCurrentResultsWordList(
            String resultsFromRecognizer,
            MainActivity.MasterState masterState,
            boolean isSpeaking) {
        overwriteCurrentResults(resultsFromRecognizer);
        String[] splitterArray = resultsFromRecognizer.split(" ");
        currentResultsWordList.clear();
        WordType wordType;
        for (String s : splitterArray) {
            if (isSpeaking) {
                wordType = WordType.IGNORED;
            } else {
                wordType = masterState == MainActivity.MasterState.EDITING ?
                        WordType.KEYWORD : WordType.NORMAL;
            }
            currentResultsWordList.add(new Word(s, wordType));
        }
        Log.d(TAG, this.toString());

        // todo - Further split words by punctuation marks returned by the recognizer.
    }

    public String getCumulativeCurrentResults() {
        return cumulativeCurrentResults;
    }

    public void overwriteCurrentResults(String cumulativeCurrentResults) {
        this.cumulativeCurrentResults = cumulativeCurrentResults;
    }

    public List<Word> getCurrentResultsWordList() {
        return currentResultsWordList;
    }

    public void clearWordList() {
        currentResultsWordList.clear();
    }

    public Word getLastWord() {
        return currentResultsWordList.get(currentResultsWordList.size() - 1);
    }

    public String getLastTwoWordsAsString() {
        int size = currentResultsWordList.size();
        if (size < 2) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(currentResultsWordList.get(size - 2).getWordString());
            sb.append(" ");
            sb.append(currentResultsWordList.get(size - 1).getWordString());
            return sb.toString();
        }
    }

    public void disregardLastTwoWords() {
        int size = currentResultsWordList.size();
        currentResultsWordList.get(size - 2).declareKeyword(true);
        currentResultsWordList.get(size - 1).declareKeyword(true);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResultsUnderEvaluation.currentResultsWordList\n");
        for (Word w : currentResultsWordList) {
            sb.append(w.getWordString() + "(" + w.getType().toString() + ") ");
        }
        return sb.toString();
    }

    public static String getWordListAsString(List<Word> list) {
        StringBuilder sb = new StringBuilder("ResultsUnderEvaluation.currentResultsWordList\n");
        for (Word w : list) {
            sb.append(w.getWordString() + "(" + w.getType().toString() + ") ");
        }
        return sb.toString();
    }
}
