package com.intricatech.autodictator;

import android.util.Log;

/**
 * Created by Bolgbolg on 11/02/2018.
 */

public enum InterpreterSelector {

    STANDARD_INTERPRETER(R.color.standardInterpreter, "DICTATE"),
    READ_INTERPRETER(R.color.readInterpreter, "READ"),
    SELECT_INTERPRETER(R.color.selectInterpreter, "SELECT");

    String[] keywords;
    int color;

    String TAG = "Interpreter Selector";

    InterpreterSelector(int col, String... keywordPossibilities) {
        color = col;
        keywords = keywordPossibilities;
    }

    InterpreterSelector queryAppropriateInterpreterSelector(
            String candidateKeyword,
            AbstractInterpreter currentInterpreter) {

        // Check for a match with all keywords for each InterpreterSelector value.
        AbstractInterpreter interpreterInUse = currentInterpreter;
        for (InterpreterSelector is : InterpreterSelector.values()) {
            for (String s : is.keywords) {
                if (candidateKeyword.toUpperCase().equals(s)) {
                    Log.d(TAG, "Keyword match detected : match == " + is.toString());
                    return is;
                }
            }
        }

        // No match.
        return InterpreterSelector.STANDARD_INTERPRETER;
    }

    AbstractInterpreter getInterpreter() {
        switch(this) {
            case STANDARD_INTERPRETER:
                return StandardInterpreter.getInstance();
            case READ_INTERPRETER:
                return ReadInterpreter.getInstance();
            case SELECT_INTERPRETER:
                break;
        }
        return StandardInterpreter.getInstance();
    }
}
