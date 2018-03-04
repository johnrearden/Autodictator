package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Sentence {

    private List<Word> wordList;
    private long storageID;
    private int indexOfAppearance;
    private int paragraphIndex;

    public Sentence(int paragraphIndex) {
        wordList = new LinkedList<>();
        storageID = -1;
        this.indexOfAppearance = paragraphIndex;
    }

    public List<Word> getWordList() {
        return wordList;
    }

    public void addWord(Word word) {
        wordList.add(word);
    }

    public String getWordListAsString() {
        StringBuilder sb = new StringBuilder();
        int size = wordList.size();
        for (Word word : wordList) {
            sb.append(word.getWordString() + " ");
        }
        sb.deleteCharAt(sb.length() - 1); // delete final trailing space.
        return sb.toString();
    }

    public long getStorageID() {
        return storageID;
    }

    public void setStorageID(long storageID) {
        this.storageID = storageID;
    }

    public int getIndexOfAppearance() {
        return indexOfAppearance;
    }

    public void setIndexOfAppearance(int indexOfAppearance) {
        this.indexOfAppearance = indexOfAppearance;
    }

    public int getParagraphIndex() {
        return paragraphIndex;
    }

    public String toString() {
        return "index : " + indexOfAppearance + ", para : " + paragraphIndex + " : " + getWordListAsString();
    }
}
