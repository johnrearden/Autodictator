package com.intricatech.autodictator;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 08/02/2018.
 */

public class Results {

    private static String TAG;

    private String previousRecognizerOutput;
    private List<Word> wordList;

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

    public void overwriteCurrentResults(String cumulativeCurrentResults) {
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

    public String getLastTwoWordsAsString() {
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
    }

    public void ignoreLastNumberOfWords(int numberOfWordsToIgnore) {
        int size = wordList.size();
        for (int i = size - numberOfWordsToIgnore; i < size; i++) {
            wordList.get(i).declareKeyword(true);
            Log.d(TAG, toString());
        }
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
