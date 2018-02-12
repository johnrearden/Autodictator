package com.intricatech.autodictator;

/**
 * Created by Bolgbolg on 11/02/2018.
 */

public abstract class AbstractInterpreter implements InterpreterState {

    static final String SWITCH_TO_EDIT_KEYWORD = "Ok so";

    @Override
    public abstract boolean interpret(ResultsUnderEvaluation resultsUnderEvaluation,
                                      String resultsFromRecognizer,
                                      MainActivity.MasterState masterState);

    public static boolean shouldSwitchToEditMode(String candidate) {
        if (candidate.equals(SWITCH_TO_EDIT_KEYWORD)
                || candidate.equals(SWITCH_TO_EDIT_KEYWORD.toLowerCase())
                || candidate.equals(SWITCH_TO_EDIT_KEYWORD.toUpperCase())) {
            return true;
        } else return false;
    }
}
