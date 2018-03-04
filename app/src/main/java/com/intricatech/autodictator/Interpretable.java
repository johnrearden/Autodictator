package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 07/02/2018.
 */

public interface Interpretable {

    /**
     * Concrete interpreters implement this method.
     * @param resultsFromRecognizer The String returned by the recognizer with the
     *                              highest probability score.
     */
    public void interpret(String resultsFromRecognizer, Results results);
}
