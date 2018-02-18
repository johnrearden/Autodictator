package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Sentence {

    private List<Word> wordList;
    private int storageID;
    private int indexOfAppearance;

    public Sentence() {
        wordList = new LinkedList<>();
        storageID = -1;
        indexOfAppearance = -1;
    }



    public List<Word> getWordList() {
        return wordList;
    }

    public void addWord(Word word) {
        wordList.add(word);
    }

    public int getStorageID() {
        return storageID;
    }

    public void setStorageID(int storageID) {
        this.storageID = storageID;
    }

    public int getIndexOfAppearance() {
        return indexOfAppearance;
    }

    public void setIndexOfAppearance(int indexOfAppearance) {
        this.indexOfAppearance = indexOfAppearance;
    }
}
