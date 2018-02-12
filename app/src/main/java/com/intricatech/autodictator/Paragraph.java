package com.intricatech.autodictator;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Paragraph {

    private List<Sentence> sentenceList;

    public Paragraph(Sentence... sentences) {
        sentenceList = Arrays.asList(sentences);
    }

    public Paragraph() {
        this(null);
    }

    public List<Sentence> getSentenceList() {
        return sentenceList;
    }
}
