package com.intricatech.autodictator;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Sentence {

    private List<Word> wordList;

    public Sentence(Word... words) {
        wordList = Arrays.asList(words);
    }

    public Sentence() {
        this(null);
    }

    public List<Word> getWordList() {
        return wordList;
    }
}
