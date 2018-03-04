package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Word {

    private String wordString;
    private WordType type;

    public Word(String wordString, WordType wordType) {
        this.wordString = wordString;
        this.type = wordType;
    }

    public String getWordString() {
        return wordString;
    }

    public void setWordString(String wordString) {
        this.wordString = wordString;
    }

    public WordType getType() {
        return type;
    }

    public void setType(WordType type) {
        this.type = type;
    }

    public void declareKeyword(boolean keyword) {
        type = WordType.KEYWORD;
    }
}
