package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Word {

    private String wordString;
    private WordType type;

    public Word(String wordString, boolean isKeyword) {
        this.wordString = wordString;
        this.type = isKeyword ? WordType.KEYWORD : WordType.NORMAL;
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

    public void setKeyword(boolean keyword) {
        type = WordType.KEYWORD;
    }
}
