package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 11/02/2018.
 */

public abstract class AbstractInterpreter implements Interpretable {

    static final String SWITCH_TO_EDIT_KEYWORD = "OK SO";

    @Override
    public abstract void interpret(String resultsFromRecognizer, Results results);

    public static boolean shouldSwitchToEditMode(String candidate) {
        if (candidate.toUpperCase().equals(SWITCH_TO_EDIT_KEYWORD)) {
            return true;
        } else {
            return false;
        }
    }
}
