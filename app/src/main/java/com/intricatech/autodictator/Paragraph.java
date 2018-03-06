package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Paragraph {

    private List<Sentence> sentenceList;
    private int index;

    public Paragraph(int index) {
        sentenceList = new LinkedList<>();
        this.index = index;
    }

    public Iterable<Sentence> getSentenceList() {
        return sentenceList;
    }

    public void addSentence(Sentence sentence) {
        sentenceList.add(sentence);
    }

    public int getIndex() {
        return index;
    }

    public Sentence getCurrentSentence() {
        return sentenceList.get(sentenceList.size() - 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Paragraph " + index + "\n");
        for (Sentence s : sentenceList) {
            sb.append(s.toString() + "\n");
        }
        return sb.toString() + "\n\n";
    }
}
