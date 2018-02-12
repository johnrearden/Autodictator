package com.intricatech.autodictator;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public class Document {

    private List<Paragraph> paragraphList;

    public Document(Paragraph... paragraphs) {
        paragraphList = Arrays.asList(paragraphs);
    }

    public Document() {
        this(null);
    }

}
