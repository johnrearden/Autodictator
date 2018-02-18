package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Paragraph {

    private List<Sentence> sentenceList;

    public Paragraph() {
        sentenceList = new LinkedList<>();
    }



    public List<Sentence> getSentenceList() {
        return sentenceList;
    }

    public void addSentence(Sentence sentence) {
        sentenceList.add(sentence);
    }
}
