package com.intricatech.autodictator;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bolgbolg on 06/03/2018.
 */

public class Selection {

    private static String TAG;
    private List<Sentence> sentenceList;

    public Selection() {
        TAG = getClass().getSimpleName();
        sentenceList = new LinkedList<>();
    }

    public void addSentence(Sentence sentence) {
        sentenceList.add(sentence);
    }

    public void addParagraph(Paragraph p) {
        for (Sentence s : p.getSentenceList()) {
            sentenceList.add(s);
        }
    }

    public Iterable<Sentence> getSentences() {
        return sentenceList;
    }

    public boolean isEmpty() {
        return sentenceList.isEmpty();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Sentence s : sentenceList) {
            sb.append(s.getSentenceAsString());
            sb.append(" ");
        }
        sb.setLength(sb.length() - 1);  // remove final trailing space.
        return sb.toString();
    }
}
